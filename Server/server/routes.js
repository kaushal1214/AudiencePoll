var express = require('express'),
	router= express.Router(),
	home = require('../controllers/home'),
	participants = require('../controllers/participants');

module.exports = function(app){
	//Home page
	router.get('/',home.index);

	//To show GUI for a particular team
	router.get('/participants/:device_id',participants.index);
	
	//To show GUI for opening voting lines
	router.get('/compare',participants.compare);

	//To show GUI for comparing two teams
	router.get('/openvoting',participants.openvoting);
	
	//TO send a JSON response for all teams
	router.get('/names',participants.names);
	
	//To register a new team
	router.post('/participants',participants.create);

	//To receive votes from the audience
	router.post('/votings/:device_id',participants.votings);

	//To delete a particular team
	router.post('/participants/:device_id/delete',participants.delete);

	//To open the voting lines
	router.post('/openvoting',participants.openVotingLine);

	//To compare the two teams
	router.post('/compare',participants.compareTeams);
	
	app.use(router);
};
