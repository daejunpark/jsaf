 // JavaScript Document
function car_getCookie(c_name){
	if (document.cookie.length>0)
	{
		c_start=document.cookie.indexOf(c_name + "=")
		if (c_start!=-1)
		{
			c_start=c_start + c_name.length+1
			c_end=document.cookie.indexOf(";",c_start)
			if (c_end==-1) c_end=document.cookie.length
			return unescape(document.cookie.substring(c_start,c_end))
		}
	}
	return ""
}

function car_setCookie(N, V, Q) {
	var L = new Date();
	var z = new Date(L.getTime() + Q * 60000);
	var domain = '';//'.ifeng.com';
	document.cookie = N + "=" + escape(V) + ";path=/;domain=" + domain + ";expires=" + z.toGMTString() + ";"
};


var arrCarCity=new Array(
['010','北京市','beijing','bj','11'],
['021','上海市','shanghai','sh','31'],
['022','天津市','tianjin','tj','12'],
['023','重庆市','chongqing','cq','50'],
['0311','河北省','hebei','hb','1300'],
['0351','山西省','shanxi','sx','1400'],
['0471','内蒙古自治区','neimenggu','nmg','1500'],
['024','辽宁省','liaoning','ln','2100'],
['0431','吉林省','jilin','jl','2200'],
['0451','黑龙江省','heilongjiang','hlj','2300'],
['025','江苏省','jiangsu','js','3200'],
['0571','浙江省','zhejiang','zj','3300'],
['0551','安徽省','anhui','ah','3400'],
['0591','福建省','fujian','fj','3500'],
['0791','江西省','jiangxi','jx','3600'],
['0531','山东省','shandong','sd','3700'],
['0371','河南省','henan','hn','4100'],
['027','湖北省','hubei','hb','4200'],
['0731','湖南省','hunan','hn','4300'],
['020','广东省','guangdong','gd','4400'],
['0771','广西壮族自治区','guangxi','gx','4500'],
['0898','海南省','hainan','hn','4600'],
['028','四川省','sichuan','sc','5100'],
['0851','贵州省','guizhou','gz','5200'],
['0871','云南省','yunnan','yn','5300'],
['0891','西藏自治区','xizang','xz','5400'],
['029','陕西省','shanxi','sx','6100'],
['0931','甘肃省','gansu','gs','6200'],
['0951','宁夏回族自治区','ningxia','nx','6400'],
['0971','青海省','qinghai','qh','6300'],
['0991','新疆维吾尔自治区','xinjiang','xj','6500'],
['0311','石家庄市','shijiazhuang','hb_sjz','1301'],
['0315','唐山市','tangshan','hb_ts','1302'],
['0335','秦皇岛','qinhuangdao','hb_qhd','1303'],
['0310','邯郸市','handan','hb_hd','1304'],
['0319','邢台市','xingtai','hb_xta','1305'],
['0312','保定市','baoding','hb_bd','1306'],
['0313','张家口','zhangjiakou','hb_zjk','1307'],
['0314','承德市','chengde','hb_cd','1308'],
['0317','沧州市','cangzhou','hb_cz','1309'],
['0316','廊坊市','langfang','hb_lf','1310'],
['0318','衡水市','hengshui','hb_hs','1311'],
['0351','太原市','taiyuan','sx_ty','1401'],
['0352','大同市','datong','sx_dt','1402'],
['0353','阳泉市','yangquan','sx_yq','1403'],
['0355','长治市','changzhi','sx_cz','1404'],
['0356','晋城市','jincheng','sx_jc','1405'],
['0349','朔州市','shuozhou','sx_sz','1406'],
['0354','晋中市','jinzhong','sx_jz','1407'],
['0359','运城市','yuncheng','sx_yc','1408'],
['0350','忻州市','xinzhou','sx_xz','1409'],
['0357','临汾市','linfen','sx_lf','1410'],
['0358','吕梁市','lvliang','sx_ll','1411'],
['0471','呼和浩特','huhehaote','nmg_hhht','1501'],
['0472','包头市','baotou','nmg_bt','1502'],
['0473','乌海市','wuhai','nmg_wh','1503'],
['0476','赤峰市','chifeng','nmg_cf','1504'],
['0475','通辽市','tongliao','nmg_tl','1505'],
['0470','呼伦贝尔','hulunbeier','nmg_hlbe','1507'],
['0477','鄂尔多斯市','eerduosi','nmg_eeds','1506'],
['0478','巴彦淖尔市','bayannaoer','nmg_byze','1508'],
['0474','乌兰察布市','wulanchabu','nmg_wlcb','1509'],
['0313','兴安盟','xinganmeng','nmg_xa','1522'],
['0479','锡林郭勒盟','xilinguole','nmg_xlgl','1525'],
['024','沈阳市','shenyang','ln_sy','2101'],
['0411','大连市','dalian','ln_dl','2102'],
['0412','鞍山市','anshan','ln_as','2103'],
['0413','抚顺市','fushun','ln_fs','2104'],
['0414','本溪市','benxi','ln_bx','2105'],
['0415','丹东市','dandong','ln_dd','2106'],
['0416','锦州市','jinzhou','ln_jz','2107'],
['0417','营口市','yingkou','ln_yk','2108'],
['0418','阜新市','fuxin','ln_fx','2109'],
['0419','辽阳市','liaoyang','ln_ly','2110'],
['0427','盘锦市','panjin','ln_pj','2111'],
['0410','铁岭市','tieling','ln_tl','2112'],
['0421','朝阳市','chaoyang','ln_cy','2113'],
['0429','葫芦岛市','huludao','ln_hld','2114'],
['0431','长春市','changchun','jl_cc','2201'],
['0432','吉林市','jilin','jl_jl','2202'],
['0434','四平市','siping','jl_sp','2203'],
['0437','辽源市','liaoyuan','jl_ly','2204'],
['0435','通化市','tonghua','jl_th','2205'],
['0439','白山市','baishan','jl_bs','2206'],
['0438','松原市','songyuan','jl_sy','2207'],
['0436','白城市','baicheng','jl_bc','2208'],
['0433','延边朝鲜族','yanbian','jl_yb','2224'],
['0451','哈尔滨市','haerbin','hlj_heb','2301'],
['0452','齐齐哈尔市','qiqihaer','hlj_qqhe','2302'],
['0467','鸡西市','jixi','hlj_jx','2303'],
['0468','鹤岗市','hegang','hlj_hg','2304'],
['0469','双鸭山市','shuangyashan','hlj_sys','2305'],
['0459','大庆市','daqing','hlj_dq','2306'],
['0458','伊春市','yichun','hlj_yc','2307'],
['0454','佳木斯市','jiamusi','hlj_jms','2308'],
['0464','七台河市','qitaihe','hlj_qth','2309'],
['0453','牡丹江市','mudanjiang','hlj_mdj','2310'],
['0456','黑河市','heihe','hlj_hh','2311'],
['0455','绥化市','suihua','hlj_sh','2312'],
['0457','大兴安岭','daxinganling','hlj_dxal','2327'],
['025','南京市','nanjing','js_nj','3201'],
['0510','无锡市','wuxi','js_wx','3202'],
['0516','徐州市','xuzhou','js_xz','3203'],
['0519','常州市','changzhou','js_cz','3204'],
['0512','苏州市','suzhou','js_sz','3205'],
['0513','南通市','nantong','js_nt','3206'],
['0518','连云港市','lianyungang','js_lyg','3207'],
['0517','淮安市','huaian','js_ha','3208'],
['0515','盐城市','yancheng','js_yc','3209'],
['0514','扬州市','yangzhou','js_yz','3210'],
['0511','镇江市','zhenjiang','js_zj','3211'],
['0523','泰州市','taizhou','js_tz','3212'],
['0527','宿迁市','suqian','js_sq','3213'],
['0571','杭州市','hangzhou','zj_hz','3301'],
['0574','宁波市','ningbo','zj_nb','3302'],
['0577','温州市','wenzhou','zj_wz','3303'],
['0573','嘉兴市','jiaxing','zj_jx','3304'],
['0572','湖州市','huzhou','zj_hz','3305'],
['0575','绍兴市','shaoxing','zj_sx','3306'],
['0579','金华市','jinhua','zj_jh','3307'],
['0570','衢州市','quzhou','zj_qz','3308'],
['0580','舟山市','zhoushan','zj_zs','3309'],
['0576','台州市','taizhou','zj_tz','3310'],
['0578','丽水市','lishui','zj_ls','3311'],
['0551','合肥市','hefei','ah_hf','3401'],
['0553','芜湖市','wuhu','ah_wh','3402'],
['0552','蚌埠市','bengbu','ah_bb','3403'],
['0554','淮南市','huainan','ah_hn','3404'],
['0555','马鞍山市','maanshan','ah_mas','3405'],
['0561','淮北市','huaibei','ah_hb','3406'],
['0562','铜陵市','tongling','ah_tl','3407'],
['0556','安庆市','anqing','ah_aq','3408'],
['0559','黄山市','huangshan','ah_hs','3410'],
['0550','滁州市','chuzhou','ah_cz','3411'],
['0558','阜阳市','fuyang','ah_fy','3412'],
['0557','宿州市','suzhou','ah_sz','3413'],
['0565','巢湖市','chaohu','ah_ch','3414'],
['0564','六安市','luan','ah_la','3415'],
['0558','亳州市','bozhou','ah_bz','3416'],
['0566','池州市','chizhou','ah_cz','3417'],
['0563','宣城市','xuancheng','ah_xc','3418'],
['0591','福州市','fuzhou','fj_fz','3501'],
['0592','厦门市','xiamen','fj_xm','3502'],
['0594','莆田市','putian','fj_pt','3503'],
['0598','三明市','sanming','fj_sm','3504'],
['0595','泉州市','quanzhou','fj_qz','3505'],
['0596','漳州市','zhangzhou','fj_zz','3506'],
['0599','南平市','nanping','fj_np','3507'],
['0597','龙岩市','longyan','fj_ly','3508'],
['0593','宁德市','ningde','fj_nd','3509'],
['0313','南昌市','nanchang','jx_nc','3601'],
['0798','景德镇市','jingdezhen','jx_jdz','3602'],
['0799','萍乡市','pingxiang','jx_px','3603'],
['0792','九江市','jiujiang','jx_jj','3604'],
['0790','新余市','xinyu','jx_xy','3605'],
['0701','鹰潭市','yingtan','jx_yt','3606'],
['0797','赣州市','ganzhou','jx_gz','3607'],
['0796','吉安市','jian','jx_ja','3608'],
['0795','宜春市','yichun','jx_yc','3609'],
['0794','抚州市','fuzhou','jx_fz','3610'],
['0793','上饶市','shangrao','jx_sr','3611'],
['0531','济南市','jinan','sd_jn','3701'],
['0532','青岛市','qingdao','sd_qd','3702'],
['0533','淄博市','zibo','sd_zb','3703'],
['0632','枣庄市','zaozhuang','sd_zz','3704'],
['0546','东营市','dongying','sd_dy','3705'],
['0535','烟台市','yantai','sd_yt','3706'],
['0536','潍坊市','weifang','sd_wf','3707'],
['0537','济宁市','jining','sd_jni','3708'],
['0538','泰安市','taian','sd_ta','3709'],
['0631','威海市','weihai','sd_wh','3710'],
['0633','日照市','rizhao','sd_rz','3711'],
['0634','莱芜市','laiwu','sd_lw','3712'],
['0539','临沂市','linyi','sd_ly','3713'],
['0534','德州市','dezhou','sd_dz','3714'],
['0635','聊城市','liaocheng','sd_lc','3715'],
['0543','滨州市','binzhou','sd_bz','3716'],
['0530','荷泽市','heze','sd_hz','3717'],
['0371','郑州市','zhengzhou','hn_zzh','4101'],
['0378','开封市','kaifeng','hn_kf','4102'],
['0379','洛阳市','luoyang','hn_ly','4103'],
['0375','平顶山市','pingdingshan','hn_pts','4104'],
['0372','安阳市','anyang','hn_ay','4105'],
['0392','鹤壁市','hebi','hn_hb','4106'],
['0373','新乡市','xinxiang','hn_xx','4107'],
['0391','焦作市','jiaozuo','hn_jz','4108'],
['0393','濮阳市','puyang','hn_py','4109'],
['0374','许昌市','xuchang','hn_xc','4110'],
['0395','漯河市','luohe','hn_lh','4111'],
['0398','三门峡市','sanmenxia','hn_smx','4112'],
['0377','南阳市','nanyang','hn_ny','4113'],
['0370','商丘市','shangqiu','hn_sq','4114'],
['0376','信阳市','xinyang','hn_xy','4115'],
['0394','周口市','zhoukou','hn_zk','4116'],
['0396','驻马店市','zhumadian','hn_zmd','4117'],
['0391','济源市','jiyuan','hn_jy','410881'],
['027','武汉市','wuhan','hb_wh','4201'],
['0714','黄石市','huangshi','hb_hs','4202'],
['0719','十堰市','shiyan','hb_sy','4203'],
['0717','宜昌市','yichang','hb_yc','4205'],
['0710','襄樊市','xiangfan','hb_xf','4206'],
['0711','鄂州市','ezhou','hb_ez','4207'],
['0724','荆门市','jingmen','hb_jm','4208'],
['0712','孝感市','xiaogan','hb_xg','4209'],
['0716','荆州市','jingzhou','hb_jz','4210'],
['0713','黄冈市','huanggang','hb_hg','4211'],
['0715','咸宁市','xianning','hb_xn','4212'],
['0722','随州市','suizhou','hb_sz','4213'],
['0718','恩施土家族苗族','enshi','hb_es','4228'],
['0728','仙桃市','xiantao','hb_xt','429004'],
['0728','潜江市','qianjiang','hb_qj','429005'],
['0728','天门市','tianmen','hb_tm','429006'],
['0719','神农架林区','shennongjia','hb_snj','429021'],
['0731','株洲市','zhuzhou','hn_zz','4302'],
['0731','湘潭市','xiangtan','hn_xt','4303'],
['0731','长沙市','changsha','hn_cs','4301'],
['0734','衡阳市','hengyang','hn_hy','4304'],
['0739','邵阳市','shaoyang','hn_sy','4305'],
['0730','岳阳市','yueyang','hn_yy','4306'],
['0736','常德市','changde','hn_cd','4307'],
['0744','张家界市','zhangjiajie','hn_zjj','4308'],
['0737','益阳市','yiyang','hn_yy','4309'],
['0735','郴州市','chenzhou','hn_cz','4310'],
['0746','永州市','yongzhou','hn_yz','4311'],
['0745','怀化市','huaihua','hn_hh','4312'],
['0738','娄底市','loudi','hn_ld','4313'],
['0743','湘西土家族苗族','xiangxi','hn_xx','4331'],
['020','广州市','guangzhou','gd_gz','4401'],
['0751','韶关市','shaoguan','gd_sg','4402'],
['0755','深圳市','shenzhen','gd_sz','4403'],
['0756','珠海市','zhuhai','gd_zh','4404'],
['0754','汕头市','shantou','gd_st','4405'],
['0757','佛山市','foshan','gd_fs','4406'],
['0750','江门市','jiangmen','gd_jm','4407'],
['0759','湛江市','zhanjiang','gd_zj','4408'],
['0668','茂名市','maoming','gd_mm','4409'],
['0758','肇庆市','zhaoqing','gd_zq','4412'],
['0752','惠州市','huizhou','gd_hz','4413'],
['0753','梅州市','meizhou','gd_mz','4414'],
['0660','汕尾市','shanwei','gd_sw','4415'],
['0762','河源市','heyuan','gd_hy','4416'],
['0662','阳江市','yangjiang','gd_yj','4417'],
['0763','清远市','qingyuan','gd_qy','4418'],
['0769','东莞市','dongguan','gd_d','4419'],
['0760','中山市','zhongshan','gd_zs','4420'],
['0768','潮州市','chaozhou','gd_sz','4451'],
['0663','揭阳市','jieyang','gd_jy','4452'],
['0766','云浮市','yunfu','gd_yf','4453'],
['0771','南宁市','nanning','gx_nn','4501'],
['0772','柳州市','liuzhou','gx_lz','4502'],
['0773','桂林市','guilin','gx_gl','4503'],
['0774','梧州市','wuzhou','gx_wz','4504'],
['0779','北海市','beihai','gx_bh','4505'],
['0770','防城港市','fangchenggang','gx_fcg','4506'],
['0777','钦州市','qinzhou','gx_qz','4507'],
['0775','贵港市','guigang','gx_gg','4508'],
['0775','玉林市','yulin','gx_yl','4509'],
['0776','百色市','baise','gx_bs','4510'],
['0774','贺州市','hezhou','gx_hz','4511'],
['0778','河池市','hechi','gx_hc','4512'],
['0772','来宾市','laibin','gx_lb','4513'],
['0771','崇左市','chongzuo','gx_cz','4514'],
['0898','海口市','haikou','hn_hk','4601'],
['0898','三亚市','sanya','hn_sy','4602'],
['028','成都市','chengdu','sc_cd','5101'],
['0813','自贡市','zigong','sc_zg','5103'],
['0812','攀枝花市','panzhihua','sc_pzh','5104'],
['0830','泸州市','luzhou','sc_lz','5105'],
['0838','德阳市','deyang','sc_dy','5106'],
['0816','绵阳市','mianyang','sc_my','5107'],
['0839','广元市','guangyuan','sc_gy','5108'],
['0825','遂宁市','suining','sc_sn','5109'],
['0832','内江市','neijiang','sc_nj','5110'],
['0833','乐山市','leshan','sc_ls','5111'],
['0817','南充市','nanchong','sc_nc','5113'],
['028','眉山市','meishan','sc_ms','5114'],
['0831','宜宾市','yibin','sc_yb','5115'],
['0826','广安市','guangan','sc_ga','5116'],
['0818','达州市','dazhou','sc_dz','5117'],
['0835','雅安市','yaan','sc_ya','5118'],
['0827','巴中市','bazhong','sc_bz','5119'],
['028','资阳市','ziyang','sc_zy','5120'],
['0837','阿坝藏族','aba','sc_ab','5132'],
['0836','甘孜藏族','ganzi','sc_gz','5133'],
['0834','凉山彝族','liangshan','sc_ls','5134'],
['0851','贵阳市','guiyang','gz_gy','5201'],
['0858','六盘水市','liupanshui','gz_lps','5202'],
['0852','遵义市','zunyi','gz_zy','5203'],
['0853','安顺市','anshun','gz_as','5204'],
['0856','铜仁','tongren','gz_tr','5222'],
['0859','黔西南布依族苗族','qianxinan','gz_qxn','5223'],
['0857','毕节区','bijiequ','gz_bj','5224'],
['0855','黔东南苗族侗族','qiandongnan','gz_qdn','5226'],
['0854','黔南布依族苗族','qiannan','gz_qn','5227'],
['0871','昆明市','kunming','yn_km','5301'],
['0874','曲靖市','qujing','yn_qj','5303'],
['0877','玉溪市','yuxi','yn_yx','5304'],
['0875','保山市','baoshan','yn_bs','5305'],
['0870','昭通市','zhaotong','yn_zt','5306'],
['0888','丽江市','lijiang','yn_lj','5307'],
['0879','普洱市','puer','yn_pe','5308'],
['0883','临沧市','lincang','yn_lc','5309'],
['0878','楚雄彝族','chuxiong','yn_cx','5323'],
['0873','红河哈尼族彝族','honghe','yn_hh','5325'],
['0876','文山壮族苗族','wenshan','yn_ws','5326'],
['0691','西双版纳傣族','xishuangbanna','yn_xsbn','5328'],
['0872','大理白族','dali','yn_dl','5329'],
['0692','德宏傣族景颇族','dehong','yn_dh','5331'],
['0886','怒江傈僳族','nujiang','yn_nj','5333'],
['0887','迪庆藏族','diqing','yn_dq','5334'],
['0891','拉萨市','lasa','xz_ls','5401'],
['0895','昌都','changdu','xz_cd','5421'],
['0893','山南','shannan','xz_sn','5422'],
['0892','日喀则','rikeze','xz_rkz','5423'],
['0896','那曲地区','naqu','xz_nq','5424'],
['0897','阿里地区','ali','xz_al','5425'],
['0894','林芝地区','linzhi','xz_lz','5426'],
['029','西安市','xian','sx_xa','6101'],
['0919','铜川市','tongchuan','sx_tc','6102'],
['0917','宝鸡市','baoji','sx_bj','6103'],
['029','咸阳市','xianyang','sx_xy','6104'],
['0913','渭南市','weinan','sx_wn','6105'],
['0911','延安市','yanan','sx_ya','6106'],
['0916','汉中市','hanzhong','sx_hz','6107'],
['0912','榆林市','yulin','sx_yl','6108'],
['0915','安康市','ankang','sx_ak','6109'],
['0914','商洛市','shangluo','sx_sl','6110'],
['0931','兰州市','lanzhou','gs_lz','6201'],
['0937','嘉峪关市','jiayuguan','gs_jyg','6202'],
['0935','金昌市','jinchang','gs_jc','6203'],
['0943','白银市','baiyin','gs_by','6204'],
['0938','天水市','tianshui','gs_ts','6205'],
['0935','武威市','wuwei','gs_ww','6206'],
['0936','张掖市','zhangye','gs_zy','6207'],
['0933','平凉市','pingliang','gs_pl','6208'],
['0937','酒泉市','jiuquan','gs_jq','6209'],
['0934','庆阳市','qingyang','gs_qy','6210'],
['0932','定西市','dingxi','gs_dx','6211'],
['0939','陇南市','longnan','gs_ln','6212'],
['0930','临夏回族','linxia','gs_lx','6229'],
['0941','甘南藏族','gannan','gs_gn','6230'],
['0971','西宁市','xining','qh_xn','6301'],
['0972','海东','haidong','qh_hd','6321'],
['0970','海北藏族','haibei','qh_hb','6322'],
['0973','黄南藏族','huangnan','qh_hn','6323'],
['0974','海南藏族','hainan','qh_hn','6325'],
['0975','果洛藏族','guoluo','qh_gl','6326'],
['0976','玉树藏族','yushu','qh_ys','6327'],
['0979','海西蒙古族藏族','haixi','qh_hx','6328'],
['0951','银川市','yinchuan','nx_yc','6401'],
['0952','石嘴山市','shizuishan','nx_szs','6402'],
['0953','吴忠市','wuzhong','nx_wz','6403'],
['0954','固原市','guyuan','nx_gy','6404'],
['0955','中卫市','zhongwei','nx_zw','6405'],
['0991','乌鲁木齐市','wulumuqi','xj_wlmq','6501'],
['0990','克拉玛依市','kelamayi','xj_klmy','6502'],
['0995','吐鲁番地区','tulufan','xj_tlf','6521'],
['0902','哈密地区','hami','xj_hm','6522'],
['0994','昌吉回族','changji','xj_cj','6523'],
['0909','博尔塔拉','boertala','xj_betl','6527'],
['0996','巴音郭楞','bayinguoleng','xj_bygl','6528'],
['0997','阿克苏地区','akesu','xj_aks','6529'],
['0908','克孜勒苏柯尔克孜','kezilesukeerkezi','xj_kzlskekz','6530'],
['0998','喀什地区','kashi','xj_ks','6531'],
['0903','和田地区','hetian','xj_ht','6532'],
['0999','伊犁哈萨克','yilihasake','xj_ylhsk','6540'],
['0901','塔城地区','tacheng','xj_tc','6542'],
['0906','阿勒泰地区','aletai','xj_alt','6543'],
['0993','石河子市','shihezi','xj_shz','659001'],
['0998','图木舒克市','tumushuke','xj_tmsk','659003'],
['0483','阿拉善盟','alashanmeng','nmg_alsm','1529']);

var openCity=new Array(
['010','北京市','beijing','bj','11'],
['021','上海市','shanghai','sh','31'],
['022','天津市','tianjin','tj','12'],
['023','重庆市','chongqing','cq','50'],
['0311','石家庄市','shijiazhuang','hb_sjz','1301'],
['0351','太原市','taiyuan','sx_ty','1401'],
['0531','济南市','jinan','sd_jn','3701'],
['0539','临沂市','linyi','sd_ly','3713'],
['0537','济宁市','jining','sd_jni','3708'],
['0543','滨州市','binzhou','sd_bz','3716'],
['0431','长春市','changchun','jl_cc','2201'],
['024','沈阳市','shenyang','ln_sy','2101'],
['0411','大连市','dalian','ln_dl','2102'],
['0451','哈尔滨市','haerbin','hlj_heb','2301'],
['0452','齐齐哈尔市','qiqihaer','hlj_qqhe','2302'],
['0459','大庆市','daqing','hlj_dq','2306'],
['0454','佳木斯市','jiamusi','hlj_jms','2308'],
['0453','牡丹江市','mudanjiang','hlj_mdj','2310'],
['025','南京市','nanjing','js_nj','3201'],
['0512','苏州市','suzhou','js_sz','3205'],
['0510','无锡市','wuxi','js_wx','3202'],
['0514','扬州市','yangzhou','js_yz','3210'],
['0519','常州市','changzhou','js_cz','3204'],
['0523','泰州市','taizhou','js_tz','3212'],
['0551','合肥市','hefei','ah_hf','3401'],
['0564','六安市','luan','ah_la','3415'],
['0371','郑州市','zhengzhou','hn_zzh','4101'],
['027','武汉市','wuhan','hb_wh','4201'],
['0731','长沙市','changsha','hn_cs','4301'],
['0760','中山市','zhongshan','gd_zs','4420'],
['0750','江门市','jiangmen','gd_jm','4407'],
['0871','昆明市','kunming','yn_km','5301'],
['0931','兰州市','lanzhou','gs_lz','6201'],
['029','西安市','xian','sx_xa','6101'],
['0991','乌鲁木齐市','wulumuqi','xj_wlmq','6501'],
['0769','东莞市','dongguan','gd_d','4419'],
['0313','南昌市','nanchang','jx_nc','3601'],
['0771','南宁市','nanning','gx_nn','4501'],
['0571','杭州市','hangzhou','zj_hz','3301'],
['0951','银川市','yinchuan','nx_yc','6401'],
['0523','泰州市','taizhou','js_tz','3212'],
['0519','常州市','changzhou','js_cz','3204'],
['0513','南通市','nantong','js_nt','3206'],
['0551','合肥市','hefei','ah_hf','3401'],
['0532','青岛市','qingdao','sd_qd','3702'],
['020','广州市','guangzhou','gd_gz','4401'],
['0533','淄博市','zibo','sd_zb','3703'],
['0851','贵阳市','guiyang','gz_gy','5201'],
['0574','宁波市','ningbo','zj_nb','3302'],
['0412','鞍山市','anshan','ln_as','2103']
);

//动态加载JS文件
function jsLoader(){
	this.load=function(url){
            //获取所有的<script>标记
            var jslst=document.getElementsByTagName("script");
            //判断指定的文件是否已经包含，如果已包含则触发onsuccess事件并返回
            for (i=0;i<jslst.length;i++){
                if (jslst[i].src && jslst[i].src.indexOf(url)!=-1){
                    this.onsuccess();
                    return;
                }
            }
            //创建script结点,并将其属性设为外联JavaScript文件
            js=document.createElement("script");
            js.type="text/javascript";
            js.src=url;
            //获取head结点，并将<script>插入到其中
            var objhead=document.getElementsByTagName("head")[0];
            objhead.appendChild(js);

            //获取自身的引用
            var self=this;
            //对于IE浏览器，使用readystatechange事件判断是否载入成功
            //对于其他浏览器，使用onload事件判断载入是否成功
            js.onload=js.onreadystatechange=function(){
                if (this.readyState && this.readyState=="loading") return;
                self.onsuccess();
            }
            js.onerror=function(){
                head.removeChild(js);
                self.onfailure();
            }
	}
}



//显示汽车地方站内容
function showCarCityplace(carcity){
try{
	var tmpname='';

		//判断是否为开放地方站
		if(openCity!=null && openCity!=undefined){
			for(var i=0;i<openCity.length;i++){
				if(openCity[i][3]==carcity){
					tmpname=openCity[i][2];
					break;
				}
			}
		}

		//IP定向是未开放地方站，显示开放地方的省会城市内容
		if(tmpname==''){
			var tmpnum='';
			for(var i=0;i<arrCarCity.length;i++){
				if(arrCarCity[i][3]==carcity){
					tmpnum=arrCarCity[i][4];
					break;
				}
			}
			var pro=carcity.replace(/\_.+/,"");
			for(var i=0;i<openCity.length;i++){
				if(/bj|sh|tj|cq/.test(pro)){
					if(openCity[i][3]==pro){
						tmpname=openCity[i][2];
						break;
					}
				}
				else{
					if(openCity[i][3].replace(/(\_).+/,"$1")==pro+"_" && openCity[i][4].substring(0,2)==tmpnum.substring(0,2)){
						tmpname=openCity[i][2];
						break;
					}
				}
			}
		}
		//省会城市也未开通的城市定向到北京
		tmpname=tmpname==undefined||tmpname==null||tmpname==''?'beijing':tmpname;

		//获取地方站信息
	var jsload=new jsLoader();
         var domain = "http://m"+Math.floor(Math.random()*4)+".ifengimg.com";
         var jsurl=domain+"/auto_city/v_i_a_b/auto_area_news_"+tmpname+".js";		jsload.load(jsurl);
		jsload.onsuccess=function(){
			var htmlstring=a_a_n_s;
			var num=htmlstring.match(/<li>/g).length;
			var arr=document.getElementById("car_city").childNodes;
			var reindex=0;
			for(var i=0;i<arr.length;i++){
				if(arr[i].innerHTML!=undefined && arr[i].innerHTML!=null && arr[i].innerHTML!=""){
					reindex++;
					if(reindex>num){
						htmlstring+="<li>"+arr[i].innerHTML+"</li>";
					}
				}
			}
			document.getElementById("car_city").innerHTML=htmlstring;
		}

}
catch(e){
//异常处理
}

}

//回调函数
function getCityInfo(locationStr){
	var carcity=/\[([^\]]+)\]/g.exec(locationStr)[1];
	carcity=carcity==undefined||carcity==null||carcity==""?"bj":carcity;
	showCarCityplace(carcity);
}

if(car_getCookie("weather_city")==undefined || car_getCookie("weather_city")==null || car_getCookie("weather_city")==""){
	document.write('<scr'+'ipt type="text/javascript" src="http://region.ifeng.com/get?format=js&callback=getCityInfo"><'+'/script>');
}
else{
	carcity=car_getCookie("weather_city");
	showCarCityplace(carcity);
}
