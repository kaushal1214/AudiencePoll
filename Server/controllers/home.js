var Models = require('../models').Participants;
var sidebar = require('../helpers/sidebar');
var fs = require('fs');
module.exports = {
	index: function(req,res){

		//Delete the Pie chart CSV file, to avoid wrong chart formation
		fs.unlink('./public/csv/ratings.csv', function(err){

		});
		var viewModel = {
			Participants: []
		};
		Models.find({},{},{sort: {timestamp:-1}},function(err,docs){
		if(err)
		{
			console.log(err);
		}
		viewModel.Participants= docs;
		sidebar(viewModel, function(viewModel){
			res.render('index',viewModel);
		});	
	});
		
		
	}
};
