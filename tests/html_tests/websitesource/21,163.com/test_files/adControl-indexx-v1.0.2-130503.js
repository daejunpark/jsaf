var adCindex = new Object();

//�ݶӿ���
adCindex.go = function(code)
{
	//alert("�ݶӣ�"+code)
	switch(code)
	{
	   case 1:
		{
			//��һ�ݶ�
			if(typeof(fcBox) != "undefined")
			{
				//����
				fcBox.go();
			}
			 if(typeof(xtBox) != "undefined")
			{
				//����
				xtBox.go();
			}
			if((typeof(fcBox) == "undefined")&&(typeof(xtBox) == "undefined"))
			{
				//û�е�һ�ݶ�
				adCindex.go(2);
			}
			break;
	   }
	   case 2:
		{
			//�ڶ��ݶ�
			if(typeof(dlBox) != "undefined")
			{
				//����
				dlBox.go();
			}
			if(typeof(scBox) != "undefined")
			{
				//��Ƶ
				scBox.go();
			}
			if(typeof(dwBox) != "undefined")
			{
				//����
				dwBox.createAD();
			}else
			{
				adCindex.go(3);
			}
			break;
	   }
	   case 3:
		{
			//�����ݶ�
			//alert("�����ݶ����!")
			break;
	   }
		break;
	}
}
adCindex.go(1);