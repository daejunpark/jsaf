var adCindex = new Object();

//梯队控制
adCindex.go = function(code)
{
	//alert("梯队："+code)
	switch(code)
	{
	   case 1:
		{
			//第一梯队
			if(typeof(fcBox) != "undefined")
			{
				//浮层
				fcBox.go();
			}
			 if(typeof(xtBox) != "undefined")
			{
				//下推
				xtBox.go();
			}
			if((typeof(fcBox) == "undefined")&&(typeof(xtBox) == "undefined"))
			{
				//没有第一梯队
				adCindex.go(2);
			}
			break;
	   }
	   case 2:
		{
			//第二梯队
			if(typeof(dlBox) != "undefined")
			{
				//对联
				dlBox.go();
			}
			if(typeof(scBox) != "undefined")
			{
				//视频
				scBox.go();
			}
			if(typeof(dwBox) != "undefined")
			{
				//底纹
				dwBox.createAD();
			}else
			{
				adCindex.go(3);
			}
			break;
	   }
	   case 3:
		{
			//第三梯队
			//alert("第三梯队输出!")
			break;
	   }
		break;
	}
}
adCindex.go(1);