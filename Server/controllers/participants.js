const Models = require('../models');
const sidebar = require('../helpers/sidebar');
const ratings= require('../helpers/ratings');
const fs = require('fs');
const json2csv = require('json2csv');
var VOTING_OPEN_FOR = {};
var COMPARE_TEAMS = {};
const sockets = require('../server');

module.exports ={
	index: function(req,res){
		var viewModel ={
			participant:{}
		}
		Models.Participants.findOne({id: {$regex: req.params.device_id}},function(err,docs){
			if(err)
			{
				console.log(err);
			}
			if(docs)
			{
				var Ratings = docs.ratings;
				if(Ratings.length>0)
				{
					ratings(Ratings,function(err,data){
						if(err) throw err;
						console.log("Function called:");
						console.log(data);

						var fields = ['ratings', 'number'];
						var myCars = [
						{
						    "ratings": "1 Star",
						    "number": data.Star1
						},
						{
						    "ratings": "2 Star",
						    "number": data.Star2
						}, {
						    "ratings": "3 Star",
						    "number": data.Star3
						  },
						{
							"ratings":"4 Star",
							"number":data.Star4
						},
						{
							"ratings":"5 Star",
							"number":data.Star5
						}];
						var csv = json2csv({ data: myCars, fields: fields });

						fs.writeFile('./public/csv/ratings.csv', csv, function(err) {
						if (err) throw err;
						console.log('file saved');
						});
					});
				}

			        viewModel.participant =docs;
				res.render('participant',viewModel);
			}
			else{
				res.redirect("/");
			}
		});

	},
	create: function(req, res){
		//res.send('The Device:created POST controller');
		var saveDevice = function(){
			var possible = "abcdefghijklmnopqrstuvwxyz0123456789";
			var devUrl= '';
			for ( var i =0; i<=6  ; i +=1)
			{
				devUrl += possible.charAt(Math.floor(Math.random()*possible.length));
			}
			Models.Participants.find({id:devUrl},function(err,docs){
				if(docs.length > 0)
				{
					saveDevice();
				}
				else
				{
					var newDev = new Models.Participants({
						name:   req.body.title,
						group:  req.body.group,
						mobile: req.body.mobile,
						id:     devUrl
					});
					newDev.save(function(err,dev){
						res.redirect('/');
					});
				}
			});

		};
		saveDevice();

	},

	names: function(req,res){

		Models.Participants.find({},function(err, docs){
			if(err){
				console.log(err);
			}
			console.log(docs);
			if(docs)
			{
				var data ={
					names:[],
					id:[]
				};
				for(var i = 0; i<docs.length;i++)
				{
					data.names.push(docs[i].name);
					data.id.push(docs[i].id);
				}
				console.log(data);

				res.json(data);

			}
			else
				res.json({});
		});
	},
	/*------------------------------------
	 * GUI to compare two teams at a time
   	 *-----------------------------------*/
	compare: function(req,res){
		var participantList = {list:{}};
		Models.Participants.find({},function(err,docs){
			if(err)
				throw err;
			if(docs)
			{
				if(COMPARE_TEAMS.team1 != null && COMPARE_TEAMS.team2 !=null)
				{
					//Compare the two teams
					var team1Data, team2Data;
					for(var i=0; i<docs.length;i++)
					{
						if(docs[i].id==COMPARE_TEAMS.team1)
							team1Data=docs[i];
						if(docs[i].id==COMPARE_TEAMS.team2)
							team2Data=docs[i];
						if(team1Data !=null && team2Data !=null)
							break;
					}
					var team1Ratings, team2Ratings;
					ratings(team1Data.ratings,function(err, data){

						try
						{
							if(err)
								throw err;
							else
							{
								var total = data.Star1*1 + data.Star2*2 + data.Star3*3 + data.Star4*4 + data.Star5*5;
								var count = data.Star1 + data.Star2 + data.Star3 + data.Star4 + data.Star5;
								team1Ratings = (total/count).toFixed(1);
								console.log("Team 1 Ratings: "+team1Ratings);

								ratings(team2Data.ratings,function(err,data){
									if(err) throw err;
									else
									{
										team2Ratings = data;
										var total = data.Star1*1 + data.Star2*2 + data.Star3*3 + data.Star4*4 + data.Star5*5;
										var count = data.Star1 + data.Star2 + data.Star3 + data.Star4 + data.Star5;
										team2Ratings = (total/count).toFixed(1);
										console.log("Team 2 Ratings: "+team2Ratings);
									}
									sockets.on('connection',function(){
										sockets.emit('result', {team1Name:team1Data.name, team2Name:team2Data.name, team1: team1Ratings, team2: team2Ratings});

									});
								});
							}
						}catch(e)
						{
							console.log(e);
							sockets.on('connection', function(){
								sockets.emit('result',{error:'No ratings available'});
							});
						}
				  });
				}
				participantList.list = docs;
				res.render('compare',participantList);
			}
			else
				res.redirect('/');
		});

	},

	/*---------------------------------------
	 * GUI to open voting line for the Teams
	 *---------------------------------------*/
	openvoting: function(req,res){
		var votingTeams = {list:{}};
		Models.Participants.find({},function(err,docs){
			if(err)
				throw err;
			if(docs)
			{
				votingTeams.list = docs;
				res.render('openvoting',votingTeams);
			}
			else
				res.redirect('/');
		});

	},

	/*-------------------------------------------------------
	 * POST service to receive the Voting Line open request
	 *------------------------------------------------------*/
	openVotingLine: function(req,res){

		//Store the IDs for Teams whose Voting Lines are open
		VOTING_OPEN_FOR = req.body;
		res.redirect('/openvoting');
		sockets.on('connection',function(){
			sockets.emit('votingsOpen',VOTING_OPEN_FOR);
		});
	},

//POST request to register ratings from the user
	votings: function(req,res,next){
		var id = req.params.device_id;

		if(VOTING_OPEN_FOR[id] != null )
		{
			var DOUBLE_ENTRY = false;
			console.log("Adding audience ratings...");
			Models.Participants.findOne({id:{$regex: req.params.device_id}},function(err,doc){

			if(err)
				throw err;
			else
			{
				if(doc)
				{
					for(var i= 0; i< doc.ratings.length;i++)
					{
						if(doc.ratings[i].MobileID == req.body.mobileid )
						{
							DOUBLE_ENTRY = true;
							console.log("User already rated this Participant");
							break;
						}
					}
					if(!DOUBLE_ENTRY)
	         			{
						console.log("Inserting ratings now...");
						Models.Participants.update({id:{$regex: req.params.device_id}},{$push:{ratings:{MobileID: req.body.mobileid, Rate: req.body.rate}}},function(err,docs){
			 			if(err) throw err;
						console.log("Ratings inserted for - " + req.params.device_id);
						});
						res.json({status:201, message:"Voting submitted!"});
					}
					else
						res.json({status:403, message:"Entry already exists. Multiple votings not allowed."});
				}
				else
					res.json({status:404, message:"User not found"});
			}
			});
		}
		else
			res.json({status:400, message:"Voting lines are closed"});
	},

	/*---------------------------------------------------
	 * POST request to get details of the comparing teams
	 *---------------------------------------------------*/
	compareTeams: function(req,res){

		const data = req.body;

		if(data.team1=='hint' || data.team2=='hint')
			COMPARE_TEAMS = {};
		else
			COMPARE_TEAMS = req.body;

		res.redirect('/compare');

	},
	delete: function(req,res){
		Models.Participants.remove({id:{$regex: req.params.device_id}},function(err,docs){
			if(err) console.log(err);
			console.log("Device has been removed.");
		});
		res.redirect('/');
	}

};
