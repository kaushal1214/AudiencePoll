/*---------------------------------------------------
* File: Server.js
* Purpose: To create a server using Express
*---------------------------------------------------*/

var app = require('express')();

//var config = require('./server/configure');

var mongoose = require('mongoose');
var server = require('http').createServer(app);
var io = require('socket.io')(server);
module.exports=io;

var config = require('./server/configure');

app.set('port',process.env.PORT || 3300);
app.set('views',__dirname + '/views');
app = config(app);

mongoose.connect("mongodb:/\/localhost/debate");
mongoose.connection.on('open',function(){
		console.log("Mongoose Connected");
});

io.on('connection', function (client){
	console.log("A client connected!");
	client.on('data',function(data){
		console.log(data)
	});

	client.on('data',function(data)	{
		console.log(data);
	});

});


server.listen(3300);


