var mongoose = require('mongoose'),
	Schema = mongoose.Schema;

var DeviceSchema =new Schema({
	name:		{type: String},
	group: 	        {type: String},
	id:		{type: String},
	mobile:		{type: Number, 'default':0},
	ratings:	[{
				_id:false,
				MobileID: {type: String, 'default':"1"},
				Rate:      {type:Number, 'default':0}
			}],
	timestamp:	{type: Date ,'default': Date.now}
});

module.exports= mongoose.model('Device',DeviceSchema);
