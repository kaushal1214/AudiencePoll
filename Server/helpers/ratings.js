module.exports = function(viewModel,callback){
		var err;
		var data = {Star1:{},Star2:{},Star3:{},Star4:{},Star5:{}};
		var Star1=0, Star2=0, Star3=0, Star4=0, Star5=0;
		if(viewModel.length>0)
		{
			for(var i =0;i<viewModel.length;i++)
			{
				switch( viewModel[i].Rate)
				{
					case 1: Star1++; break;
					case 2: Star2++; break;
					case 3: Star3++; break;
					case 4: Star4++; break;
					case 5: Star5++; break;
					default:
				}
			}
			data.Star1=Star1;
			data.Star2=Star2;
			data.Star3=Star3;
			data.Star4=Star4;
			data.Star5=Star5;
		}
		else
			err="Error: Array is null";
		callback(err,data);
};
