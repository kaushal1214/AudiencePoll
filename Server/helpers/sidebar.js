var Stats = require('./stats'),
	Help = require('./help');

module.exports = function(viewModel,callback){
		var obj = viewModel.Participants;
		console.log(viewModel);
		viewModel.sidebar= {
			stats: Stats(),
//			total: obj.length,
			help: Help.devices()
		};
		callback(viewModel);
};
