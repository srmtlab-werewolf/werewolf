package org.aiwolf.myAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.aiwolf.client.base.player.AbstractMedium;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class MyMedium extends AbstractMedium {

	MyRoleAssignPlayer mrap;
	int i,k,s;
	
	
	
	public MyMedium(MyRoleAssignPlayer mrap) {
		super();
		this.mrap = mrap;
		
	}
	

	

	
	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting){
		super.initialize(gameInfo, gameSetting);
		mrap.MyNum = gameInfo.getAgent().getAgentIdx();
		mrap.MaxNum = mrap.NowNum = gameSetting.getPlayerNum();
		mrap.VILLAGERNum =  mrap.AliveVILLAGERNum = gameSetting.getRoleNum(Role.VILLAGER);
		mrap.SEERNum = mrap.AliveSEERNum = gameSetting.getRoleNum(Role.SEER);
		mrap.MEDIUMNum = mrap.AliveMEDIUMNum = gameSetting.getRoleNum(Role.MEDIUM);
		mrap.BODYGUARDNum = mrap.AliveBODYGUARDNum = gameSetting.getRoleNum(Role.BODYGUARD);
		mrap.WEREWOLFNum = mrap.AliveWEREWOLFNum = gameSetting.getRoleNum(Role.WEREWOLF);
		mrap.POSSESSEDNum = mrap.AlivePOSSESSEDNum = gameSetting.getRoleNum(Role.POSSESSED);
		mrap.myrole = gameInfo.getRole();
		mrap.MEDIUMDetectNum = 1;
		for(int L = 0; L < mrap.MaxNum; L++){
			mrap.DetectWPO[L] = 0;
		}//他役職の人外露出数を記録
		for(i = 1; i <= mrap.MaxNum;i++){
		if(mrap.lasttalkvot.get(Agent.getAgent(i)) == null){
			mrap.lasttalkvot.put(Agent.getAgent(i), new ArrayList<Utterance>());
		}}
		for(i = 1; i <= mrap.MaxNum;i++){
			for(int Hi = 1; Hi <= mrap.MaxNum;Hi++){
				mrap.logit[i][Hi] = 0.0;
			}	
		}
		for( i = 1;i <= mrap.MaxNum;i++){
			if(i != mrap.MyNum){
			mrap.MyThinkJob[i][0] = 8.0 / (mrap.MaxNum - 1.0);
			mrap.MyThinkJob[i][1] = 1.0 / (mrap.MaxNum - 1.0);
			mrap.MyThinkJob[i][2] = 0.0;
			mrap.MyThinkJob[i][3] = 1.0 / (mrap.MaxNum - 1.0);
			mrap.MyThinkJob[i][4] = 3.0 / (mrap.MaxNum - 1.0);
			mrap.MyThinkJob[i][5] = 1.0 / (mrap.MaxNum - 1.0);
			
			}
			else{
				mrap.MyThinkJob[i][0] = 0;
				mrap.MyThinkJob[i][1] = 0;
				mrap.MyThinkJob[i][2] = 1.0;
				mrap.MyThinkJob[i][3] = 0;
				mrap.MyThinkJob[i][4] = 0;
				mrap.MyThinkJob[i][5] = 0;
			}
			}
		for(i=0;i < 16;i++){
			for(k= 0;k < 6;k++){
				mrap.ThinkSEERJob[i][k] = mrap.MyThinkJob[i][k]; 
			}}
		for(i=0;i < 16;i++){
			for(k= 0;k < 16;k++){
				mrap.HaveTrust[i][k] = 1 / (1 + Math.exp(-mrap.logit[i][k]));//信頼の基準値を0.5とする
			}}
		
		
		output();
	}
	
	
	
	
	public void output(){
		double sum;
		double sum1[] = new double[16];;
		if(mrap.SeerNum != 0){
			System.out.printf("ThinkSEERJob:from:Agent%d\n",mrap.SeerNum);
			for(i = 1; i <= mrap.MaxNum; i++){
				System.out.printf("AGT%2d|",i);}
			System.out.printf("\n");
			for(i = 0; i < 6; i++){
				sum = 0;
				for(k = 1; k < 16; k++){
			System.out.printf("%.3f|",mrap.ThinkSEERJob[k][i]);
			sum = sum + mrap.ThinkSEERJob[k][i];
			sum1[k] = sum1[k] + mrap.ThinkSEERJob[k][i]; 
				}
				System.out.printf("合計%.3f", sum);
				System.out.println("\n");}
			for(k = 1; k <= mrap.MaxNum; k++){
				System.out.printf("%.3f|",sum1[k]);}
			System.out.printf("\n");	
		}
		else if(mrap.SeerNum == 0){
		System.out.printf("mrap.MyThinkJob\n");
		for(i = 1; i <= mrap.MaxNum; i++){
			System.out.printf("AGT%2d|",i);}
		System.out.printf("\n");
		for(i = 0; i < 6; i++){
			sum = 0;
			for(k = 1; k < 16; k++){
		System.out.printf("%.3f|",mrap.MyThinkJob[k][i]);
		sum = sum + mrap.MyThinkJob[k][i];
		sum1[k] = sum1[k] + mrap.MyThinkJob[k][i]; 
			}
			System.out.printf("合計%.3f", sum);
			System.out.println("\n");}
		for(k = 1; k <= mrap.MaxNum; k++){
			System.out.printf("%.3f|",sum1[k]);}
		System.out.printf("\n");}
		for(i = 0; i < mrap.judgeList.size(); i++){
		System.out.printf("%s:%s",mrap.judgeAgentList.get(i),mrap.judgeList.get(i).getResult());
		System.out.printf("\n");}
		System.out.printf("占い師COリスト");
		System.out.println(mrap.SeerCOAgent);
		System.out.printf("生きている占い師COリスト");
		System.out.println(mrap.AliveSeerCOAgent);
		for(i = 0; i < mrap.SeerCOAgent.size(); i++){
			System.out.printf("%s:%d\n", mrap.SeerCOAgent.get(i),mrap.DetectWPO[mrap.SeerCOAgent.get(i).getAgentIdx()]);
		}
		System.out.printf("偽占いCOリスト");
		System.out.println(mrap.fakeSeerCOAgent);
		System.out.printf("生きている偽占いCOリスト");
		System.out.println(mrap.AlivefakeSeerCOAgent);
		for(i = 0; i < mrap.fakeSeerCOAgent.size(); i++){
			System.out.printf("%s:%d\n", mrap.fakeSeerCOAgent.get(i),mrap.DetectWPO[mrap.fakeSeerCOAgent.get(i).getAgentIdx()]);
		}
		System.out.printf("偽霊能COリスト");
		System.out.println(mrap.fakeMEDIUMAgent);
		System.out.printf("生きている偽霊能COリスト");
		System.out.println(mrap.AlivefakeMEDIUMAgent);
		
		System.out.printf("見つけた狼リスト");
		System.out.println(mrap.WEREWOLFAgent);
		System.out.printf("生きている見つけた狼リスト");
		System.out.println(mrap.AliveWEREWOLFAgent);
		System.out.printf("見つけた人外の数:%d\n",mrap.DetectWP);
		System.out.printf("信頼値表（仮）\n");
		
		for(i = 1; i < 16; i++){
			for(k = 1; k < 16; k++){
				System.out.printf("%.3f|", mrap.HaveTrust[k][i]);}
			System.out.printf("\n");
		}
	}
	
	
	
	
	@Override
	public void finish() {
		double sum;
		double sum1[] = new double[16];;
		for(i = 1; i <= mrap.MaxNum; i++){
			System.out.printf("AGT%2d|",i);}
		System.out.printf("\n");
		for(i = 0; i < 6; i++){
			sum = 0;
			for(k = 1; k < 16; k++){
		System.out.printf("%.3f|",mrap.MyThinkJob[k][i]);
		sum = sum + mrap.MyThinkJob[k][i];
		sum1[k] = sum1[k] + mrap.MyThinkJob[k][i]; 
			}
			System.out.printf("合計%.3f", sum);
			System.out.println("\n");}
		for(k = 1; k <= mrap.MaxNum; k++){
			System.out.printf("%.3f|",sum1[k]);}
		System.out.printf("\n");
		mrap.AllAgent.addAll(mrap.gameInfo1.getAgentList());
		mrap.usefinish();
		System.out.printf("予想\n");
		for(k = 1; k <= mrap.MaxNum; k++){
			switch(mrap.gameInfo1.getRoleMap().get(Agent.getAgent(k))){
			case VILLAGER:
				System.out.printf("VILLA|");
				break;
			case SEER:
				System.out.printf("SEER |");
				break;
			case MEDIUM:
				System.out.printf("MEDIU|");
				break;
			case BODYGUARD:
				System.out.printf("BODYG|");
				break;
			case WEREWOLF:
				System.out.printf("WEREW|");
				break;
			case POSSESSED:
				System.out.printf("POSSE|");
				break;
			default:
				break;
			}}
		System.out.printf("結果\n");
		for(i = 0; i < mrap.judgeList.size(); i++){
			System.out.printf("%s:%s",mrap.judgeAgentList.get(i),mrap.judgeList.get(i).getResult());
			System.out.printf("\n");}
			System.out.printf("占い師COリスト");
			System.out.println(mrap.SeerCOAgent);
			System.out.printf("生きている占い師COリスト");
			System.out.println(mrap.AliveSeerCOAgent);
			System.out.printf("偽占いCOリスト");
			System.out.println(mrap.fakeSeerCOAgent);
			System.out.printf("生きている偽占いCOリスト");
			System.out.println(mrap.AlivefakeSeerCOAgent);
			System.out.printf("偽霊能COリスト");
			System.out.println(mrap.fakeMEDIUMAgent);
			System.out.printf("生きている偽霊能COリスト");
			System.out.println(mrap.AlivefakeMEDIUMAgent);
			System.out.printf("見つけた狼リスト");
			System.out.println(mrap.WEREWOLFAgent);
			System.out.printf("生きている見つけた狼リスト");
			System.out.println(mrap.AliveWEREWOLFAgent);
			System.out.printf("見つけた人外の数:%d\n",mrap.DetectWP);
			System.out.printf("信頼値表（仮）\n");
			
			for(i = 1; i < 16; i++){
				for(k = 1; k < 16; k++){
					System.out.printf("%.3f|", mrap.HaveTrust[k][i]);}
				System.out.printf("\n");
			}
	}
	
    public void SeUp(Agent agt){//引数の占い師の情報をmrap.ThinkSEERJobに反映させる
    	int MeI;
    	int Mk;
    	for(MeI = 0; MeI < mrap.div.get(agt).size();MeI++){
    		if(!mrap.judgeAgentList.contains(mrap.div.get(agt).get(MeI).getTarget())){
    	if(mrap.div.get(agt).get(MeI).getResult() == Species.HUMAN){
    	mrap.ThinkSEERJob[mrap.div.get(agt).get(MeI).getTarget().getAgentIdx()][4] = 0.0;}
    	else if(mrap.div.get(agt).get(MeI).getResult() == Species.WEREWOLF){
    		if(!mrap.fakeSeerCOAgent.contains(mrap.div.get(agt).get(MeI)) && !mrap.SeerCOAgent.contains(mrap.div.get(agt).get(MeI)) && !mrap.fakeMEDIUMAgent.contains(mrap.div.get(agt).get(MeI))){
    			mrap.SeDetectWP++;
    		}
    		for(Mk = 0; Mk < 6; Mk++){
    		mrap.ThinkSEERJob[mrap.div.get(agt).get(MeI).getTarget().getAgentIdx()][Mk] = 0.0;
    		}
    		mrap.ThinkSEERJob[mrap.div.get(agt).get(MeI).getTarget().getAgentIdx()][4] = 1.0;
    	}
    	}	
    	}
    	
    	
    }
	
	
	
	void think(int nowday) {//daystartで呼び出す役職共通部分
		if(mrap.nowday >= 2){//二日目以降は死者がでるので
			System.out.printf("Think");
			System.out.println(mrap.gameInfo1.getExecutedAgent());
			mrap.ExecutedAgent.add(mrap.gameInfo1.getExecutedAgent());
			if(mrap.AliveWEREWOLFAgent.contains(mrap.gameInfo1.getExecutedAgent())){//処刑されたのが狼ならば
				mrap.AliveWEREWOLFAgent.remove(mrap.gameInfo1.getExecutedAgent());//生きている狼のリストから消す
			}
			if(mrap.AlivefakeSeerCOAgent.contains(mrap.gameInfo1.getExecutedAgent())){//偽占い師
				mrap.AlivefakeSeerCOAgent.remove(mrap.gameInfo1.getExecutedAgent());
			}
			if(mrap.AliveSeerCOAgent.contains(mrap.gameInfo1.getExecutedAgent())){//占い師
				mrap.AliveSeerCOAgent.remove(mrap.gameInfo1.getExecutedAgent());
			}
			if(mrap.AliveMEDIUMCOAgent.contains(mrap.gameInfo1.getExecutedAgent())){//霊能CO
				mrap.AliveMEDIUMCOAgent.remove(mrap.gameInfo1.getExecutedAgent());}
			if(mrap.AlivefakeMEDIUMAgent.contains(mrap.gameInfo1.getExecutedAgent())){//また、偽霊能なら
					mrap.AlivefakeMEDIUMAgent.remove(mrap.gameInfo1.getExecutedAgent());
				}
			
			mrap.NowNum--;
			if(mrap.vot.get(mrap.nowday - 1) == null){//今日の日付の前の日の投票結果はまだ入ってないだろうので
				mrap.vot.put(mrap.nowday - 1, new ArrayList<Vote>());//新しい日付の完成
			}
			mrap.vot.get(mrap.nowday - 1).addAll(mrap.gameInfo1.getVoteList());//投票結果を入れる
			for(i = 0; i < mrap.vot.get(mrap.nowday - 1).size(); i++){//投票結果をそれぞれ処理
				if(mrap.vot.get(mrap.nowday - 1).get(i).getAgent() != getMe()){
			mrap.CHT(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),mrap.vot.get(mrap.nowday - 1).get(i).getTarget().getAgentIdx(),-mrap.WV);}//自分に投票した相手から自分への信頼度を下げる
			if(mrap.vot.get(mrap.nowday - 1).get(i).getTarget() == getMe() && mrap.isComingOut == true){//カミングアウトした自分に投票する相手の人外値を上昇
				mrap.UJP(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),Role.WEREWOLF,mrap.WV);
				mrap.UJP(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),Role.POSSESSED,mrap.WV);}
			
			for(s = 0; s < mrap.SeerCOAgent.size();s++){
				
				for(k = 0; k < mrap.div.get(mrap.SeerCOAgent.get(s)).size();k++){
				if(mrap.div.get(mrap.SeerCOAgent.get(s)).get(k).getTarget() == mrap.vot.get(mrap.nowday - 1).get(i).getTarget() && mrap.div.get(mrap.SeerCOAgent.get(s)).get(k).getResult() == Species.HUMAN){
					mrap.CHT(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),mrap.SeerCOAgent.get(s).getAgentIdx(),-mrap.WV);
				}
				else if(mrap.div.get(mrap.SeerCOAgent.get(s)).get(k).getTarget() == mrap.vot.get(mrap.nowday - 1).get(i).getTarget() && mrap.div.get(mrap.SeerCOAgent.get(s)).get(k).getResult() == Species.WEREWOLF){
					mrap.CHT(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),mrap.SeerCOAgent.get(s).getAgentIdx(),mrap.WV);
				}
				}}//占い師の白に投票すると言った場合
			for(s = 0; s < mrap.fakeSeerCOAgent.size();s++){
				
				for(k = 0; k < mrap.div.get(mrap.fakeSeerCOAgent.get(s)).size();k++){
				if(mrap.div.get(mrap.fakeSeerCOAgent.get(s)).get(k).getTarget() == mrap.vot.get(mrap.nowday - 1).get(i).getTarget() && mrap.div.get(mrap.fakeSeerCOAgent.get(s)).get(k).getResult() == Species.HUMAN){
					mrap.CHT(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),mrap.fakeSeerCOAgent.get(s).getAgentIdx(),-mrap.WV);
				}
				else if(mrap.div.get(mrap.fakeSeerCOAgent.get(s)).get(k).getTarget() == mrap.vot.get(mrap.nowday - 1).get(i).getTarget() && mrap.div.get(mrap.fakeSeerCOAgent.get(s)).get(k).getResult() == Species.WEREWOLF){
					mrap.CHT(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),mrap.fakeSeerCOAgent.get(s).getAgentIdx(),mrap.WV);
				}
				}}//偽占い師の白に投票すると言った場合
			if(mrap.WEREWOLFAgent.contains(mrap.vot.get(mrap.nowday -1).get(i).getTarget())){
				mrap.DJP(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),Role.WEREWOLF,0.2);
				mrap.DJP(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),Role.POSSESSED,0.2);
			}//狼に投票している人物の人狼確率を下げる
			if(mrap.WEREWOLFAgent.contains(mrap.vot.get(mrap.nowday -1).get(i).getAgent())){
				mrap.DJP(mrap.vot.get(mrap.nowday - 1).get(i).getTarget().getAgentIdx(),Role.WEREWOLF,0.2);
				mrap.DJP(mrap.vot.get(mrap.nowday - 1).get(i).getTarget().getAgentIdx(),Role.POSSESSED,0.2);
			}//狼が投票した先の人狼確率を下げる
			}
			if(mrap.gameInfo1.getAttackedAgent() != null){
				if(mrap.AlivefakeSeerCOAgent.contains(mrap.gameInfo1.getAttackedAgent())){//偽占い師
					mrap.AlivefakeSeerCOAgent.remove(mrap.gameInfo1.getAttackedAgent());
				}
				if(mrap.AliveMEDIUMCOAgent.contains(mrap.gameInfo1.getAttackedAgent())){//霊能CO
					mrap.AliveMEDIUMCOAgent.remove(mrap.gameInfo1.getAttackedAgent());}
				if(mrap.AlivefakeMEDIUMAgent.contains(mrap.gameInfo1.getAttackedAgent())){//また、偽霊能なら
						mrap.AlivefakeMEDIUMAgent.remove(mrap.gameInfo1.getAttackedAgent());
				}
			mrap.AttackedAgent.add(mrap.gameInfo1.getAttackedAgent());
			mrap.MyThinkJob[mrap.gameInfo1.getAttackedAgent().getAgentIdx()][4] = 0;
			mrap.CleanJob(mrap.gameInfo1.getAttackedAgent().getAgentIdx());
			mrap.ChangeJob(mrap.gameInfo1.getAttackedAgent().getAgentIdx(),100,100,100,100);
			mrap.NowNum--;
				if(mrap.BGA > 0){
				mrap.BGA -= 1; 
				}
			}
			else{
				mrap.BGA = 2;
			}
			
			for(i = 0; i < mrap.vot.get(mrap.nowday -1).size(); i++){
				if(mrap.lasttalkvot.get(mrap.vot.get(mrap.nowday - 1).get(i).getAgent()).size() > 0){
			if(mrap.lasttalkvot.get(mrap.vot.get(mrap.nowday - 1).get(i).getAgent()).get(0).getTarget() != mrap.vot.get(mrap.nowday -1).get(i).getTarget()){//投票先が発言と違ったら
				mrap.UJP(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),Role.WEREWOLF,0.2);
			}}}
			for(i = 1;i <= mrap.MaxNum;i++){//毎日初期化
				mrap.lasttalkvot.get(Agent.getAgent(i)).clear();
			}
			
		}
	}
	
	
	
	
	@Override
	public void dayStart(){
		super.dayStart();
		mrap.commondayStart();
		mrap.isVote = false;
		mrap.MM = 1;
		rp = 1;
		for(i=0;i < 16;i++){
			for(k= 0;k < 16;k++){
			mrap.prelogit[i][k] = mrap.logit[i][k];//前日までの信頼度をここに保存しておく 
			}}
		if(mrap.MyState == 0){
			
			if(mrap.nowday >= 2){
				mrap.judgeList.add(mrap.gameInfo1.getMediumResult());
				mrap.judgeAgentList.add(mrap.gameInfo1.getMediumResult().getTarget());
				System.out.printf("日にち%d\n", mrap.nowday);
			if(mrap.gameInfo1.getMediumResult().getResult() == Species.HUMAN){
				System.out.printf("結果人間\n");
				mrap.WhiteAgent.add(mrap.gameInfo1.getMediumResult().getTarget());
						mrap.MyThinkJob[mrap.gameInfo1.getMediumResult().getTarget().getAgentIdx()][4] = 0.0;
						System.out.printf("番号%d\n", mrap.gameInfo1.getMediumResult().getTarget().getAgentIdx());
						mrap.CleanJob(mrap.gameInfo1.getMediumResult().getTarget().getAgentIdx());}
			else if(mrap.gameInfo1.getMediumResult().getResult() == Species.WEREWOLF){
				System.out.printf("狼発見\n");
				if(mrap.nowday >= 3){
					for(int day1 = (mrap.nowday - 2); day1 >= 1; day1--){
						for(i = 0; i < mrap.vot.get(day1).size(); i++){
					if(mrap.WEREWOLFAgent.contains(mrap.vot.get(day1).get(i).getTarget())){
						mrap.DJP(mrap.vot.get(day1).get(i).getAgent().getAgentIdx(),Role.WEREWOLF,0.2);
						mrap.DJP(mrap.vot.get(day1).get(i).getAgent().getAgentIdx(),Role.POSSESSED,0.2);
					}//狼に投票している人物の人狼確率を下げる
					if(mrap.WEREWOLFAgent.contains(mrap.vot.get(day1).get(i).getAgent())){
						mrap.DJP(mrap.vot.get(day1).get(i).getTarget().getAgentIdx(),Role.WEREWOLF,0.2);
						mrap.DJP(mrap.vot.get(day1).get(i).getTarget().getAgentIdx(),Role.POSSESSED,0.2);
					}//狼が投票した先の人狼確率を下げる
						}}}
				mrap.BlackAgent.add(mrap.gameInfo1.getMediumResult().getTarget());
				mrap.WEREWOLFAgent.add(mrap.gameInfo1.getMediumResult().getTarget());
				
				if(mrap.gameInfo1.getMediumResult().getResult() == Species.WEREWOLF && !mrap.DIVWEREAgent.contains(mrap.gameInfo1.getMediumResult().getTarget())){
					mrap.DIVWEREAgent.add(mrap.gameInfo1.getMediumResult().getTarget());
				}
				if(mrap.fakeSeerCOAgent.contains(mrap.gameInfo1.getMediumResult().getTarget())){
					
				}
				else if(mrap.SeerCOAgent.contains(mrap.gameInfo1.getMediumResult().getTarget()) && !mrap.fakeSeerCOAgent.contains(mrap.gameInfo1.getMediumResult().getTarget())){
					mrap.SeerCOAgent.remove(mrap.gameInfo1.getMediumResult().getTarget());
					mrap.AliveSeerCOAgent.remove(mrap.gameInfo1.getMediumResult().getTarget());
					mrap.fakeSeerCOAgent.add(mrap.gameInfo1.getMediumResult().getTarget());
					mrap.AlivefakeSeerCOAgent.add(mrap.gameInfo1.getMediumResult().getTarget());
					if(mrap.SeerCOAgent.size() + mrap.fakeSeerCOAgent.size() == 1){
						mrap.DetectWP++;}
				}
				else{
					mrap.DetectWP++;
				}
				for(i = 0;i <= 5; i++){
						mrap.MyThinkJob[mrap.gameInfo1.getMediumResult().getTarget().getAgentIdx()][i] = 0;}
				mrap.MyThinkJob[mrap.gameInfo1.getMediumResult().getTarget().getAgentIdx()][4] = 1.0;
				mrap.WEREWOLFDetectNum++;}
			mrap.ChangeJob(mrap.gameInfo1.getMediumResult().getTarget().getAgentIdx(),100,100,100,100);
			if(mrap.judgeAgentList.size() > 0){
				int JH, JG;
				for(JH = 0; JH < mrap.SeerCOAgent.size(); JH++){
					for(JG = 0; JG < mrap.div.get(mrap.SeerCOAgent.get(JH)).size(); JG++){
				if(mrap.div.get(mrap.SeerCOAgent.get(JH)).get(JG).getTarget() == mrap.gameInfo1.getMediumResult().getTarget()){
				if(mrap.gameInfo1.getMediumResult().getResult() != mrap.div.get(mrap.SeerCOAgent.get(JH)).get(JG).getResult()){//自分の霊能と占い結果が違ったら
					Agent agtx = mrap.SeerCOAgent.get(JH);
					mrap.MyThinkJob[mrap.SeerCOAgent.get(JH).getAgentIdx()][1] = 0;
					mrap.CleanJob(mrap.SeerCOAgent.get(JH).getAgentIdx());
					mrap.ChangeJob(mrap.SeerCOAgent.get(JH).getAgentIdx(),100,100,100,100);
					mrap.BlackAgent.add(mrap.SeerCOAgent.get(JH));
					mrap.SeerCOAgent.remove(mrap.SeerCOAgent.get(JH));
					mrap.AliveSeerCOAgent.remove(agtx);
					mrap.fakeSeerCOAgent.add(agtx);
					mrap.AlivefakeSeerCOAgent.add(agtx);
					System.out.printf("結果違う%d\n",agtx.getAgentIdx());
					break;
					}//占い師の発言が違っていたら占い師の値を下げる
				else if(mrap.gameInfo1.getMediumResult().getResult() == mrap.div.get(mrap.SeerCOAgent.get(JH)).get(JG).getResult()){//自分の霊能と結果が同じであれば
					mrap.UJP(mrap.SeerCOAgent.get(JH).getAgentIdx(),Role.SEER,mrap.TWI);
					mrap.ChangeJob(mrap.SeerCOAgent.get(JH).getAgentIdx(),100,100,100,100);
					System.out.printf("結果同じ%d\n", mrap.SeerCOAgent.get(JH).getAgentIdx());
					}//占い師の結果が霊能結果と同じなら霊能者の占い師の値を上げる
			
				}
				
					}
				}
			}
		}
			
		}
		think(mrap.nowday);
		output();
	}
	
	public void SeerCO1(){
		for(int M1 = 0; M1 < mrap.SeerCOAgent.size();M1++){
			
			for(int M2 = 0; M2 < mrap.SeerCOAgent.size();M2++){
				if(M1 != M2){
					mrap.HaveTrust[mrap.SeerCOAgent.get(M1).getAgentIdx()][mrap.SeerCOAgent.get(M2).getAgentIdx()] = 0.0;
				}}
			for(int M2 = 0; M2 < mrap.fakeSeerCOAgent.size();M2++){
				mrap.HaveTrust[mrap.SeerCOAgent.get(M1).getAgentIdx()][mrap.fakeSeerCOAgent.get(M2).getAgentIdx()] = 0.0;
			}
			}
		for(int M1 = 0; M1 < mrap.fakeSeerCOAgent.size();M1++){
			TON[DEN] = mrap.fakeSeerCOAgent.get(M1).getAgentIdx();
			DEN++;
			for(int M2 = 0; M2 < mrap.fakeSeerCOAgent.size();M2++){
				
				if(M1 != M2){
					mrap.HaveTrust[mrap.fakeSeerCOAgent.get(M1).getAgentIdx()][mrap.fakeSeerCOAgent.get(M2).getAgentIdx()] = 0.0;
				}}
			for(int M2 = 0; M2 < mrap.SeerCOAgent.size();M2++){
				
				mrap.HaveTrust[mrap.fakeSeerCOAgent.get(M1).getAgentIdx()][mrap.SeerCOAgent.get(M2).getAgentIdx()] = 0.0;
			}
			}
		for(int M1 = 0; M1 < mrap.SeerCOAgent.size();M1++){
			TON[DEN] = mrap.SeerCOAgent.get(M1).getAgentIdx();
			DEN++;
		}
	}
	int TON[] = new int[4];
	int DEN = 0;
	
	void MyMEDIUMUPD(Talk talk, Utterance utterance){
		int DIVTF;//占い結果が合致していたかどうかの確認
		int TAN;
		DEN = 0;
		if(!talk.getAgent().equals(getMe())){
			switch (utterance.getTopic()){
			case COMINGOUT:
				TAN = utterance.getTarget().getAgentIdx(); //TalkAgentNum
				if(utterance.getRole() == Role.SEER && !mrap.fakeSeerCOAgent.contains(talk.getAgent()) && !mrap.SeerCOAgent.contains(talk.getAgent())){
					if(mrap.div.get(utterance.getTarget()) == null){
						mrap.div.put(utterance.getTarget(), new ArrayList<Utterance>());
					}
					RestartUPD(TAN, Role.SEER);
					if(mrap.FirstSeerCODay == 100){
						mrap.FirstSeerCODay = mrap.nowday;}
					mrap.SeerCOAgent.add(utterance.getTarget());
					mrap.AliveSeerCOAgent.add(utterance.getTarget());
					if(mrap.COAgent.contains(utterance.getTarget())){//すでに他でCOしている人物がCOしてきたら，人外度を上昇させる
						mrap.UJP(TAN,Role.POSSESSED,0.4);
						mrap.UJP(TAN,Role.WEREWOLF,0.4);
					}
					else{//他でCOしてなくてまだリストがなければ作成
					mrap.COAgent.add(utterance.getTarget());}
					
					if(mrap.div.get(utterance.getTarget()) == null){
						mrap.div.put(utterance.getTarget(), new ArrayList<Utterance>());
					}
					if((mrap.SeerCOAgent.size()  + mrap.fakeSeerCOAgent.size()) == 1){//占いCO一人目なら
						mrap.PCJ(TAN,Role.VILLAGER,0.0);
						mrap.PCJ(TAN,Role.MEDIUM,0.0);
						mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
					mrap.ChangeJob(TAN,100,100,100,100);}
					else if((mrap.SeerCOAgent.size()  + mrap.fakeSeerCOAgent.size()) == 2){
						mrap.DetectWP++;
						mrap.PCJ(TAN,Role.VILLAGER,0.0);
						mrap.PCJ(TAN,Role.MEDIUM,0.0);
						mrap.PCJ(TAN,Role.BODYGUARD,0.0);
						SeerCO1();
						mrap.CleanJob(TAN);
						mrap.ChangeJob(TAN,TON[0],100,100,100);}
					else if((mrap.SeerCOAgent.size()  + mrap.fakeSeerCOAgent.size()) == 3){
						mrap.DetectWP++;
						mrap.PCJ(TAN,Role.VILLAGER,0.0);
						mrap.PCJ(TAN,Role.MEDIUM,0.0);
						mrap.PCJ(TAN,Role.BODYGUARD,0.0);
						SeerCO1();
					mrap.CleanJob(TAN);
						mrap.ChangeJob(TAN,TON[0],TON[1],100,100);}
					else if((mrap.SeerCOAgent.size()  + mrap.fakeSeerCOAgent.size()) == 4){
						mrap.DetectWP++;
						mrap.PCJ(TAN,Role.VILLAGER,0.0);
						mrap.PCJ(TAN,Role.MEDIUM,0.0);
						mrap.PCJ(TAN,Role.BODYGUARD,0.0);
						SeerCO1();
					mrap.CleanJob(TAN);
						mrap.ChangeJob(TAN,TON[0],TON[1],TON[2],100);}
					if((mrap.SeerCOAgent.size()  + mrap.fakeSeerCOAgent.size()) >= 2){
						int MC, MD;
						for(MC = 1; MC <= mrap.MaxNum; MC++){
							mrap.DetectWPO[MC]++;//二人以上占い師がいれば一人以外は偽物だと全員がわかる
							if(mrap.SeerCOAgent.contains(Agent.getAgent(MC))){//占い師で
								for(MD = 0;MD < mrap.div.get(Agent.getAgent(MC)).size();MD++){//すでに占っている
									if(mrap.div.get(Agent.getAgent(MC)).get(MD).getTarget() == utterance.getTarget() && mrap.div.get(Agent.getAgent(MC)).get(MD).getResult() == Species.WEREWOLF){
							mrap.DetectWPO[MC]--;//占い師が先に占っていて狼だったらマイナス１して調整
									}}}
							if(mrap.fakeSeerCOAgent.contains(Agent.getAgent(MC))){//占い師で
								for(MD = 0;MD < mrap.div.get(Agent.getAgent(MC)).size();MD++){//すでに占っている
									if(mrap.div.get(Agent.getAgent(MC)).get(MD).getTarget() == utterance.getTarget() && mrap.div.get(Agent.getAgent(MC)).get(MD).getResult() == Species.WEREWOLF){
							mrap.DetectWPO[MC]--;//占い師が先に占っていて狼だったらマイナス１して調整
									}}}
							}
						
					}
					
				}//占い師が出たらその職業値を人狼と狂人，占い師以外０にする
				if(utterance.getRole() == Role.MEDIUM && !mrap.fakeMEDIUMAgent.contains(talk.getAgent())){
					if(mrap.inq.get(utterance.getTarget()) == null){
						mrap.inq.put(utterance.getTarget(), new ArrayList<Utterance>());
					}
					RestartUPD(TAN, Role.MEDIUM);
					if(mrap.FirstMEDIUMCODay == 100){
						mrap.FirstMEDIUMCODay = mrap.nowday;}
					if(mrap.COAgent.contains(utterance.getTarget())){//すでに他でCOしている人物がCOしてきたら，人外度を上昇させる
						mrap.UJP(TAN,Role.POSSESSED,0.4);
						mrap.UJP(TAN,Role.WEREWOLF,0.4);
					}
					else{//他でCOしてなければリストに追加
						mrap.COAgent.add(utterance.getTarget());}
					if(mrap.inq.get(utterance.getTarget()) == null){
						mrap.inq.put(utterance.getTarget(), new ArrayList<Utterance>());
					}
						mrap.fakeMEDIUMAgent.add(talk.getAgent());
						mrap.AlivefakeMEDIUMAgent.add(talk.getAgent());
					
				if(mrap.fakeMEDIUMAgent.size() == 1){//霊能CO一人目なら
					mrap.DetectWP++;
					mrap.HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
					mrap.PCJ(TAN,Role.VILLAGER,0.0);
					mrap.PCJ(TAN,Role.SEER,0.0);
					mrap.PCJ(TAN,Role.MEDIUM,0.0);
					mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
					mrap.ChangeJob(TAN,100,100,100,100);}
				else if(mrap.fakeMEDIUMAgent.size() == 2){//霊能CO二人目なら
					mrap.DetectWP++;
					mrap.HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
					for(int M1 = 0; M1 < mrap.fakeMEDIUMAgent.size();M1++){
						for(int M2 = 0; M2 < mrap.fakeMEDIUMAgent.size();M2++){
							if(M1 != M2){
								mrap.HaveTrust[mrap.fakeMEDIUMAgent.get(M1).getAgentIdx()][mrap.fakeMEDIUMAgent.get(M2).getAgentIdx()] = 1.0;
							}}}
					mrap.PCJ(TAN,Role.VILLAGER,0.0);
					mrap.PCJ(TAN,Role.SEER,0.0);
					mrap.PCJ(TAN,Role.MEDIUM,0.0);
					mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
					mrap.ChangeJob(TAN,mrap.fakeMEDIUMAgent.get(0).getAgentIdx(),100,100,100);}
				else if(mrap.fakeMEDIUMAgent.size() == 3){//霊能CO三人目なら
					mrap.DetectWP++;
					mrap.HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
					for(int M1 = 0; M1 < mrap.fakeMEDIUMAgent.size();M1++){
						for(int M2 = 0; M2 < mrap.fakeMEDIUMAgent.size();M2++){
							if(M1 != M2){
								mrap.HaveTrust[mrap.fakeMEDIUMAgent.get(M1).getAgentIdx()][mrap.fakeMEDIUMAgent.get(M2).getAgentIdx()] = 1.0;
							}}}
					mrap.PCJ(TAN,Role.VILLAGER,0.0);
					mrap.PCJ(TAN,Role.SEER,0.0);
					mrap.PCJ(TAN,Role.MEDIUM,0.0);
					mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
					mrap.ChangeJob(TAN,mrap.fakeMEDIUMAgent.get(0).getAgentIdx(),mrap.fakeMEDIUMAgent.get(1).getAgentIdx(),100,100);}
				else if(mrap.fakeMEDIUMAgent.size() == 4){//霊能CO４人目なら
					mrap.DetectWP++;
					mrap.HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
					for(int M1 = 0; M1 < mrap.fakeMEDIUMAgent.size();M1++){
						for(int M2 = 0; M2 < mrap.fakeMEDIUMAgent.size();M2++){
							if(M1 != M2){
								mrap.HaveTrust[mrap.fakeMEDIUMAgent.get(M1).getAgentIdx()][mrap.fakeMEDIUMAgent.get(M2).getAgentIdx()] = 1.0;
							}}}
					mrap.PCJ(TAN,Role.VILLAGER,0.0);
					mrap.PCJ(TAN,Role.SEER,0.0);
					mrap.PCJ(TAN,Role.MEDIUM,0.0);
					mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
					mrap.ChangeJob(TAN,mrap.fakeMEDIUMAgent.get(0).getAgentIdx(),mrap.fakeMEDIUMAgent.get(1).getAgentIdx(),100,100);}
				
				if(mrap.fakeMEDIUMAgent.size() >= 1){
					int MC, MD;
					for(MC = 1; MC <= mrap.MaxNum; MC++){
						mrap.DetectWPO[MC]++;//二人以上霊能者（自分含む）がいれば一人以外は偽物だと全員がわかる
						if(mrap.fakeSeerCOAgent.contains(Agent.getAgent(MC))){//占い師で
							for(MD = 0;MD < mrap.div.get(Agent.getAgent(MC)).size();MD++){
								if(mrap.div.get(Agent.getAgent(MC)).get(MD).getTarget() == utterance.getTarget() && mrap.div.get(Agent.getAgent(MC)).get(MD).getResult() == Species.WEREWOLF){
						mrap.DetectWPO[MC]--;//占い師が先に占っていて狼だったらマイナス１して調整
								}}}
						
						}
					
				}
				
					}
				if(utterance.getRole() == Role.BODYGUARD && !mrap.BODYGUARDCOAgent.contains(talk.getAgent())){
					if(mrap.COAgent.contains(utterance.getTarget())){//すでに他でCOしている人物がCOしてきたら，人外度を上昇させる
						mrap.UJP(TAN,Role.POSSESSED,0.4);
						mrap.UJP(TAN,Role.WEREWOLF,0.4);
					}
					else{//他でCOしてなければリストに追加
						mrap.COAgent.add(utterance.getTarget());}
					mrap.BODYGUARDCOAgent.add(utterance.getTarget());
					if(mrap.BODYGUARDCOAgent.size() == 1){
						mrap.PCJ(TAN,Role.VILLAGER,0.0);
						mrap.PCJ(TAN,Role.SEER,0.0);
						mrap.PCJ(TAN,Role.MEDIUM,0.0);
						mrap.CleanJob(TAN);
					mrap.ChangeJob(TAN,100,100,100,100);}
					if(mrap.BODYGUARDCOAgent.size() == 2){
						for(int M1 = 0; M1 < mrap.BODYGUARDCOAgent.size();M1++){
							for(int M2 = 0; M2 < mrap.BODYGUARDCOAgent.size();M2++){
								if(M1 != M2){
									mrap.HaveTrust[mrap.BODYGUARDCOAgent.get(M1).getAgentIdx()][mrap.BODYGUARDCOAgent.get(M2).getAgentIdx()] = 1.0;
								}}}
						mrap.PCJ(TAN,Role.VILLAGER,0.0);
						mrap.PCJ(TAN,Role.SEER,0.0);
						mrap.PCJ(TAN,Role.BODYGUARD,0.0);
						mrap.CleanJob(TAN);
						mrap.ChangeJob(utterance.getTarget().getAgentIdx(),mrap.BODYGUARDCOAgent.get(0).getAgentIdx(),100,100,100);}
				}
				if(utterance.getRole() == Role.POSSESSED){
					mrap.UJP(TAN,Role.POSSESSED,0.2);
					mrap.UJP(TAN,Role.WEREWOLF,0.5);
				}
				if(utterance.getRole() == Role.WEREWOLF){
					mrap.UJP(TAN,Role.POSSESSED,0.7);
				}
				break;
			case DIVINED:
				if(!mrap.fakeSeerCOAgent.contains(talk.getAgent()) && !mrap.SeerCOAgent.contains(talk.getAgent())){//まだCOしてないのに占い結果報告なんてしてきたら
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.1);
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
				}//人外の値上昇
				DIVTF = mrap.MEDIadddiv(talk.getAgent(), utterance);
				if(DIVTF == 1 && !mrap.fakeSeerCOAgent.contains(talk.getAgent()) && mrap.SeerCOAgent.contains(talk.getAgent())){//まだ偽だと分かっていない人の霊能結果が食い違っていた場合
					mrap.BlackAgent.add(talk.getAgent());
					mrap.SeerCOAgent.remove(talk.getAgent());
					mrap.AliveSeerCOAgent.remove(talk.getAgent());
					mrap.fakeSeerCOAgent.add(talk.getAgent());
					mrap.AlivefakeSeerCOAgent.add(talk.getAgent());}
				RestartUPD(talk.getAgent().getAgentIdx(), Role.SEER);
				break;
			case INQUESTED://霊能結果報告
				if(!mrap.fakeMEDIUMAgent.contains(talk.getAgent()) && !mrap.MEDIUMCOAgent.contains(talk.getAgent())){//まだCOしてないのに霊能結果報告なんてしてきたら
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.1);
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
				}//人外の値上昇
				TAN = utterance.getTarget().getAgentIdx(); 
				mrap.addinq(talk.getAgent(),utterance);
				RestartUPD(talk.getAgent().getAgentIdx(), Role.MEDIUM);
				break;
				
			case ESTIMATE:
				mrap.DayTalkNum[mrap.nowday][talk.getAgent().getAgentIdx()]++;
				if(utterance.getRole() == Role.WEREWOLF || utterance.getRole() == Role.POSSESSED){
					mrap.CHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),-mrap.TWE);}
				else{
					mrap.CHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),mrap.TWE);
				}
				break;
			case GUARDED:
				if(!mrap.BODYGUARDCOAgent.contains(talk.getAgent())){//まだCOしてないのに霊能結果報告なんてしてきたら
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.1);
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
				}//人外の値上昇
				if(mrap.BGA > 0){
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.BODYGUARD,(mrap.BGA / 2.0) * mrap.TWG);
				}
				break;
			case VOTE:
				mrap.DayTalkNum[mrap.nowday][talk.getAgent().getAgentIdx()]++;
				mrap.CHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),-mrap.TWV);
				if(mrap.lasttalkvot.get(talk.getAgent()).size() >= 1){//既にこの日投票発言をしていたら
					mrap.lasttalkvot.get(talk.getAgent()).clear();
					mrap.lasttalkvot.get(talk.getAgent()).add(utterance);
				}
				else{
					mrap.lasttalkvot.get(talk.getAgent()).add(utterance);
				}
				if(utterance.getTarget() == getMe() && mrap.isComingOut == true){//カミングアウトした自分に投票する相手の人外値を上昇
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,mrap.TWV);
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,mrap.TWV);}
				
				
				break;
			case AGREE:
				mrap.DayTalkNum[mrap.nowday][talk.getAgent().getAgentIdx()]++;
				mrap.CHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),mrap.TWA);
				break;
			case DISAGREE:
				mrap.DayTalkNum[mrap.nowday][talk.getAgent().getAgentIdx()]++;
				mrap.CHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),-mrap.TWDA);
				break;
			case SKIP:
				mrap.DJP(talk.getAgent().getAgentIdx(), Role.VILLAGER, 0.05);
				break;
				default:
					break;
			}}
	}
	
	void RestartUPD(int Rn, Role rerole){//他のエージェントが役職持ちだと分かった時に役職持ちの状態でその日の最初から
		int RS;
		for(RS = 1; RS <= 15; RS++){
		mrap.HaveTrust[Rn][RS] = 1 / (1 + Math.exp(-mrap.prelogit[Rn][RS]));//評価を当日の最初まで巻き戻す
		}
		for(RS = 0; RS < mrap.daytalkList.get(mrap.nowday).size() - 1; RS++){
			Utterance utterance = new Utterance(mrap.daytalkList.get(mrap.nowday).get(RS).getContent());
		switch(rerole){
		case SEER:
			mrap.OtSeerUPD(mrap.daytalkList.get(mrap.nowday).get(RS),utterance,Rn);
			break;
		case MEDIUM:
			mrap.OtMEDIUMUPD(mrap.daytalkList.get(mrap.nowday).get(RS),utterance,Rn);
			break;
		default:
			
			break;
		
		}
			
		}
		switch(rerole){
		case SEER:
			mrap.HaveTrust[Rn][getMe().getAgentIdx()] = 0;
		for(RS = 0; RS < mrap.SeerCOAgent.size(); RS++){
			mrap.HaveTrust[Rn][mrap.SeerCOAgent.get(RS).getAgentIdx()] = 0;
		}
		for(RS = 0; RS < mrap.fakeSeerCOAgent.size(); RS++){
			mrap.HaveTrust[Rn][mrap.fakeSeerCOAgent.get(RS).getAgentIdx()] = 0;
		}
		break;
		case MEDIUM:
			for(RS = 0; RS < mrap.fakeMEDIUMAgent.size(); RS++){
				mrap.HaveTrust[Rn][mrap.fakeMEDIUMAgent.get(RS).getAgentIdx()] = 0;
			}	
			default:
				break;
				
		}
	}
	
	
	
	

	
	
	@Override
	public void update(GameInfo gameInfo) {//
		super.update(gameInfo);
		mrap.gameInfo1 = gameInfo;
		mrap.SeerNum = 0;
		mrap.SeDetectWP = mrap.DetectWP;//発見した数を自分で見つけた数に初期化
		List<Talk> talkList = gameInfo.getTalkList();//今日の会話を記録する
		//System.out.printf("alltalksize%d\n",Allmrap.daytalkList.size());
		//List<Judge> judge1 = new ArrayList<Judge>();
		//Judge judge2 = mrap.gameInfo1.getMediumResult();
		//PP = 1;
		

		for(i=0;i < 16;i++){
			for(k= 0;k < 6;k++){
				mrap.ThinkSEERJob[i][k] = mrap.MyThinkJob[i][k]; 
			}}
		//System.out.println("�肢����" + judge1.get(mrap.nowday).getDay()+"���" + judge1.get(mrap.nowday).getAgent() + judge1.get(mrap.nowday).getResult());
		for(int i = mrap.readTalkNum; i< talkList.size(); i++){
			Talk talk = talkList.get(i);
			mrap.AlldaytalkList.add(talk);
			mrap.daytalkList.get(mrap.nowday).add(talk);
			
			Utterance utterance = new Utterance(talk.getContent());
			/*for(int Si = 1; Si <= mrap.MaxNum; Si++){
				if(getMe() == Agent.getAgent(Si)){
					
				}
				else if(getMe() != Agent.getAgent(Si)){
					
				}
			}*/
			
			
			
				MyMEDIUMUPD(talk,utterance);//自分の処理を最初に行う
				
			
			for(int M = 1; M < 16; M++){
				if(getMe().getAgentIdx() != M){
			
			if(mrap.fakeSeerCOAgent.contains(Agent.getAgent(M))){
				mrap.OtSeerUPD(talk,utterance,M);
			}
			else if(mrap.MEDIUMCOAgent.contains(Agent.getAgent(M)) || mrap.fakeMEDIUMAgent.contains(Agent.getAgent(M))){
				mrap.OtMEDIUMUPD(talk,utterance,M);
			}
			else if(mrap.BODYGUARDCOAgent.contains(Agent.getAgent(M))){
			    mrap.OtBODYGUARDUPD(talk,utterance,M);	
			}
			else{
				mrap.OtVILLAGERUPD(talk,utterance,M);
			}
			}
			}
			mrap.readTalkNum++;
		}//ここまでトークの整理
		if(mrap.myrole != Role.SEER){
		if(mrap.SeerCOAgent.size() > 0){
		for(i = 0; i < mrap.SeerCOAgent.size(); i++){
		if(mrap.BOS < mrap.MyThinkJob[mrap.SeerCOAgent.get(i).getAgentIdx()][1]){//占い師の信頼度が高ければ
			SeUp(mrap.SeerCOAgent.get(i));
			mrap.SeerNum = mrap.SeerCOAgent.get(i).getAgentIdx();
		}
		}}
		}
		
	} //update 
	int rp;//roopNum
	@Override
	public String talk() {
		String voteTalk;
		if(!mrap.isComingOut){
			for(Judge judge: getMyJudgeList()){
				if(judge.getResult() == Species.WEREWOLF){
					String comingoutTalk = TemplateTalkFactory.comingout(getMe(), getMyRole());
					mrap.isComingOut = true;
					return comingoutTalk;
				}}
			if((mrap.fakeMEDIUMAgent.size() > 0 || mrap.nowday >= mrap.RAN) && rp == 1 && mrap.SeerCOAgent.size() > 0){
				return Talk.OVER;
				}
			else if((mrap.fakeMEDIUMAgent.size() > 0 || mrap.nowday >= mrap.RAN) && rp == 1 && mrap.SeerCOAgent.size() == 0){
				String comingoutTalk = TemplateTalkFactory.comingout(getMe(), getMyRole());
				mrap.isComingOut = true;
				return comingoutTalk;
				}
			else if((mrap.fakeMEDIUMAgent.size() > 0 || mrap.nowday >= mrap.RAN) && rp == 2 && mrap.SeerCOAgent.size() > 0){
				for(i = 0;i < mrap.DIVWEREAgent.size(); i++){
					if(!mrap.fakeMEDIUMAgent.contains(mrap.DIVWEREAgent.get(i)) && !mrap.SeerCOAgent.contains(mrap.DIVWEREAgent.get(i))){
					for(k = 0; k < mrap.SeerCOAgent.size(); k++){
						for(s = 0; s < mrap.div.get(mrap.SeerCOAgent.get(k) ).size(); s++){
						if(mrap.div.get(mrap.SeerCOAgent.get(k) ).get(s).getResult() == Species.WEREWOLF &&  mrap.div.get(mrap.SeerCOAgent.get(k) ).get(s).getTarget() == mrap.DIVWEREAgent.get(i)){
							return Talk.OVER;
						}
						}}
						}
				}//すでに占いCOがあり，そのうちの誰かが占い結果報告で占い師か霊能者以外に黒出している場合
				String comingoutTalk = TemplateTalkFactory.comingout(getMe(), getMyRole());
				mrap.isComingOut = true;
				return comingoutTalk;//占い師が出ても黒が出ていなければそのままCOすることとする．
				
				}
			else if((mrap.fakeMEDIUMAgent.size() > 0 || mrap.nowday >= mrap.RAN) && rp >= 3){
				String comingoutTalk = TemplateTalkFactory.comingout(getMe(), getMyRole());
			mrap.isComingOut = true;
			return comingoutTalk;
			}
			
		}
		else{
			for(Judge judge:getMyJudgeList()){
				if(!mrap.myToldJudgeList.contains(judge)){
					String resultTalk = TemplateTalkFactory.inquested(judge.getTarget(), judge.getResult());
					mrap.myToldJudgeList.add(judge);
					return resultTalk;
				}}}
		double TP = 0;
		int TN = 1;
		if(mrap.SeerNum != 0){
		for(int TJ = 1; TJ <= mrap.MaxNum; TJ++){
		if(TP < mrap.ThinkSEERJob[TJ][4] && mrap.gameInfo1.getAliveAgentList().contains(Agent.getAgent(TJ))){//生きている人間で占い師を用いて狼が見つかっていたら
			TP = mrap.ThinkSEERJob[TJ][4];
			TN = TJ;
		}
		
		}}
		int BS = 0;//０の時は誰も黒なし，１の時は誰かが黒を出した
		int BN = 0;//黒を出された番号
		for(i = 0;i < mrap.SeerCOAgent.size(); i++){
			for(k = 0; k < mrap.div.get(mrap.SeerCOAgent.get(i)).size(); k++){
				if(mrap.gameInfo1.getAliveAgentList().contains(mrap.div.get(mrap.SeerCOAgent.get(i) ) ) && mrap.div.get(mrap.SeerCOAgent.get(i) ).get(k).getResult() == Species.WEREWOLF && BS == 0){
					BS = 1;
					BN = mrap.div.get(mrap.SeerCOAgent.get(i)).get(k).getTarget().getAgentIdx();
				}//真偽のわかっていない占い師が誰かに黒を出した時
			}
		}
		if(mrap.AliveWEREWOLFAgent.size() > 0 && !mrap.isVote){//まだ生きている分かっている狼がいれば
			System.out.printf("とりあえず狼にいれよう\n");
			mrap.Voteagt = randomSelect(mrap.AliveWEREWOLFAgent);
			voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
			mrap.isVote = true;
			return voteTalk;
		}
		else if(TP >= 1.0 && !mrap.isVote){//まだ生きている狼っぽい人物がいたら
			System.out.printf("占い師決めうちで狼に入れよう\n");
			mrap.Voteagt = Agent.getAgent(TN);
			voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
			mrap.isVote = true;
			return voteTalk;
		}
		else if(BS == 1.0 && !mrap.isVote){//まだ生きている狼っぽい人物がいたら
			System.out.printf("真偽の分からない占い師からの黒に入れて色を見よう\n");
			mrap.Voteagt = Agent.getAgent(BN);
			voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
			mrap.isVote = true;
			return voteTalk;
		}
		else if(mrap.DetectWP >= 4 && !mrap.isVote){//人外を４以上（普通なら４で止まる）見つけた時、ただ既に見つけた狼は処刑した状態なはず
			System.out.printf("人外全露出じゃないですか？\n");
			System.out.printf("%d:%s",mrap.DetectWP,mrap.isVote);
			if(mrap.MEDIUMCOAgent.size() >= 2 && mrap.AlivefakeMEDIUMAgent.size() > 0){//偽霊能に投票
				mrap.isVote = true;
				mrap.Voteagt = randomSelect(mrap.AlivefakeMEDIUMAgent);
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
			else if(mrap.fakeSeerCOAgent.size() >= 2 && mrap.AlivefakeSeerCOAgent.size() > 0){//偽占い師に投票
				mrap.Voteagt = randomSelect(mrap.AlivefakeSeerCOAgent);
				mrap.isVote = true;
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
			else if(mrap.SeerCOAgent.size() >= 2 && mrap.AliveSeerCOAgent.size() > 0){//占いグレーに投票
				mrap.isVote = true;
				mrap.Voteagt = randomSelect(mrap.AliveSeerCOAgent);
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
			mrap.isVote = true;
		}
		else if(mrap.DetectWP <= 3 && !mrap.isVote && mrap.fakeMEDIUMAgent.size() >= 1 && mrap.AlivefakeMEDIUMAgent.size() >= 1 && mrap.nowday >= 3){//霊ロラ始めまたは完遂
			System.out.printf("霊ロラしようぜ\n");
			if(mrap.AlivefakeMEDIUMAgent.size() > 0){
				mrap.isVote = true;
				mrap.Voteagt = randomSelect(mrap.AlivefakeMEDIUMAgent);
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
		}
		else if(mrap.DetectWP <= 3 && !mrap.isVote && mrap.fakeMEDIUMAgent.size() >= 2 && mrap.AlivefakeMEDIUMAgent.size() >= 1 && mrap.nowday >= 2){//霊ロラ始めまたは完遂
			System.out.printf("霊ロラしようぜ\n");
			if(mrap.AlivefakeMEDIUMAgent.size() > 0){
				mrap.isVote = true;
				mrap.Voteagt = randomSelect(mrap.AlivefakeMEDIUMAgent);
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
		}
		else if(mrap.DetectWP <= 3 && !mrap.isVote && (mrap.fakeSeerCOAgent.size() + mrap.SeerCOAgent.size()) >= 3 && (mrap.AlivefakeSeerCOAgent.size() + mrap.AliveSeerCOAgent.size()) > 1 && mrap.nowday >= 3){//占いロラ
			System.out.printf("占いロラしようぜ\n");
			if(mrap.AlivefakeSeerCOAgent.size() > 0){
			mrap.isVote = true;
			mrap.Voteagt = randomSelect(mrap.AlivefakeSeerCOAgent);
			voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
			return voteTalk;}
			if(mrap.AliveSeerCOAgent.size() > 0){
				mrap.isVote = true;
				mrap.Voteagt = randomSelect(mrap.AliveSeerCOAgent);
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
		}
		else if(mrap.DetectWP <= 3 && !mrap.isVote && (mrap.fakeSeerCOAgent.size() + mrap.SeerCOAgent.size()) >= 4 && (mrap.AlivefakeSeerCOAgent.size() + mrap.AliveSeerCOAgent.size()) > 1 && mrap.nowday >= 2){//占いロラ
			System.out.printf("占いロラしようぜ\n");
			if(mrap.AlivefakeSeerCOAgent.size() > 0){
			mrap.isVote = true;
			mrap.Voteagt = randomSelect(mrap.AlivefakeSeerCOAgent);
			voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
			return voteTalk;}
			if(mrap.AliveSeerCOAgent.size() > 0){
				mrap.isVote = true;
				mrap.Voteagt = randomSelect(mrap.AliveSeerCOAgent);
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
		}
		
		return Talk.OVER;
	}

	@Override
	public Agent vote() {
		//List<Agent> voteCandidates = new ArrayList<Agent>();
		double VoteJob[][] = new double[16][6];
		if(mrap.SeerNum == 0){//現在信頼できそうな占い師がいればその結果を反映、
		for(i=0;i < 16;i++){
			for(k= 0;k < 6;k++){
				VoteJob[i][k] = mrap.MyThinkJob[i][k]; 
			}}}
		else if(mrap.SeerNum != 0){
			for(i=0;i < 16;i++){
				for(k= 0;k < 6;k++){
					VoteJob[i][k] = mrap.ThinkSEERJob[i][k]; 
				}}}
		double sumT[] = new double[16];
		double sumX;
		List<Agent> AliveList = new ArrayList<Agent>();
		AliveList.addAll(mrap.gameInfo1.getAgentList());
		AliveList.remove(getMe());
		for(i = 0; i < AliveList.size();i++){
			sumT[AliveList.get(i).getAgentIdx()] = 0;//初期化
			for(k = 0; k <AliveList.size(); k++){
				if(k != i){//当人から当人への評価は除く
			sumT[AliveList.get(i).getAgentIdx()] = sumT[AliveList.get(i).getAgentIdx()] + mrap.HaveTrust[AliveList.get(k).getAgentIdx()][AliveList.get(i).getAgentIdx()]; 
				}}}
		for(i = 0; i < 4; i++){
			if(AliveList.size() > 2){
				sumX = 100;
				int RR = 1;
				for(k = 0;k < AliveList.size();k++){
					if(sumX > sumT[AliveList.get(k).getAgentIdx()]){
						sumX = sumT[AliveList.get(k).getAgentIdx()];
						RR = AliveList.get(k).getAgentIdx();
					}
				}
				mrap.UJP(RR,Role.POSSESSED, (1 - (sumX / (AliveList.size() - 1))) * 0.3);
				mrap.UJP(RR,Role.WEREWOLF, (1 - (sumX / (AliveList.size() - 1))) * 0.3);
				AliveList.remove(Agent.getAgent(RR));
			}//全体からの信頼度の低い４人を見つけて自分の評価に反映
		}
		output();
		List<Agent> whiteAgent = new ArrayList<Agent>();
		List<Agent> onewhiteAgent = new ArrayList<Agent>();
		List<Agent> SeervoteAgent = new ArrayList<Agent>();
		List<Agent> blackAgent = new ArrayList<Agent>();
		List<Agent> oneblackAgent = new ArrayList<Agent>();
		List<Agent> fakeSeerCOvoteAgent = new ArrayList<Agent>();
		List<Agent> fakeMEDIUMvoteAgent = new ArrayList<Agent>();
		List<Agent> whiteSeerAgent = new ArrayList<Agent>();
		List<Agent> whitefakeSeerAgent = new ArrayList<Agent>();
		List<Agent> whiteMEDIUMAgent = new ArrayList<Agent>();
		if(mrap.SeerNum >= 1){
		for(Utterance judge: mrap.div.get(Agent.getAgent(mrap.SeerNum))){
if(getLatestDayGameInfo().getAliveAgentList().contains(judge.getTarget())){
    if(mrap.fakeSeerCOAgent.contains(judge.getTarget())){   
	switch (judge.getResult()){
        case HUMAN:
        	whiteSeerAgent.add(judge.getTarget());
        			break;
        case WEREWOLF:
        	blackAgent.add(judge.getTarget());
        	break;}}
    if(mrap.SeerCOAgent.contains(judge.getTarget())){   
    	switch (judge.getResult()){
            case HUMAN:
            	whitefakeSeerAgent.add(judge.getTarget());
            			break;
            case WEREWOLF:
            	blackAgent.add(judge.getTarget());
            	break;}}
    else if(mrap.fakeMEDIUMAgent.contains(judge.getTarget())){
    	switch (judge.getResult()){
        case HUMAN:
        	whiteMEDIUMAgent.add(judge.getTarget());
        			break;
        case WEREWOLF:
        	blackAgent.add(judge.getTarget());
        	break;}}
    else{
    	switch (judge.getResult()){
        case HUMAN:
        	whiteAgent.add(judge.getTarget());
        			break;
        case WEREWOLF:
        	blackAgent.add(judge.getTarget());
        	break;}
    }
}

		}}
		
		if(mrap.fakeMEDIUMAgent.size() > 0){
			for(int i =0; i < mrap.fakeMEDIUMAgent.size(); i++){
				if(getLatestDayGameInfo().getAliveAgentList().contains(mrap.fakeMEDIUMAgent.get(i)) && !blackAgent.contains(mrap.fakeMEDIUMAgent.get(i)) && !whiteMEDIUMAgent.contains(mrap.fakeMEDIUMAgent.get(i))){
					fakeMEDIUMvoteAgent.add(mrap.fakeMEDIUMAgent.get(i));
				}
			}
		}//偽霊能者が偽霊能白や狼に入っていなければ偽霊能に格納
		if(mrap.SeerCOAgent.size() > 0){
			for(int i = 0; i < mrap.SeerCOAgent.size(); i++){
			if(getLatestDayGameInfo().getAliveAgentList().contains(mrap.SeerCOAgent.get(i)) && !blackAgent.contains(mrap.SeerCOAgent.get(i)) && !whiteSeerAgent.contains(mrap.SeerCOAgent.get(i))){
				SeervoteAgent.add(mrap.SeerCOAgent.get(i));
			}
			}}//占い師が狼や占い白に入っていなければ占いグレーに格納
		if(mrap.fakeSeerCOAgent.size() > 0){
			for(int i = 0; i < mrap.fakeSeerCOAgent.size(); i++){
			if(getLatestDayGameInfo().getAliveAgentList().contains(mrap.fakeSeerCOAgent.get(i)) && !blackAgent.contains(mrap.fakeSeerCOAgent.get(i)) && !whitefakeSeerAgent.contains(mrap.fakeSeerCOAgent.get(i))){
			fakeSeerCOvoteAgent.add(mrap.fakeSeerCOAgent.get(i));
			}}}//偽占い師が狼や占い白に入っていなければ
		List<Agent> voteCandidates = new ArrayList<Agent>();
		List<Agent> voteAllCandidates = new ArrayList<Agent>();
		voteCandidates.addAll(getLatestDayGameInfo().getAliveAgentList());
		//他に投票先がなければグレーにランダムで投票
		voteCandidates.remove(getMe());
		voteCandidates.removeAll(whiteAgent);
		if(whiteMEDIUMAgent.size() > 0){
		voteCandidates.removeAll(whiteMEDIUMAgent);}
		if(whiteSeerAgent.size() > 0){
		voteCandidates.removeAll(whiteSeerAgent);}
		if(fakeSeerCOvoteAgent.size() > 0){
		voteCandidates.removeAll(fakeSeerCOvoteAgent);}
		if(whitefakeSeerAgent.size() > 0){
			voteCandidates.removeAll(whitefakeSeerAgent);}
		if(SeervoteAgent.size() > 0){
		voteCandidates.removeAll(SeervoteAgent);}
		if(blackAgent.size() > 0){
		voteCandidates.removeAll(blackAgent);}
		if(fakeMEDIUMvoteAgent.size() > 0){
		voteCandidates.removeAll(fakeMEDIUMvoteAgent);}
		voteAllCandidates.addAll(voteCandidates);
		int LW, HW;
		for(LW = 0; LW < mrap.fakeSeerCOAgent.size(); LW++){
			for(HW = 0; HW < mrap.div.get(mrap.fakeSeerCOAgent.get(LW)).size();HW++){
			if(voteAllCandidates.contains(mrap.div.get(mrap.fakeSeerCOAgent.get(LW)).get(HW).getTarget()) && mrap.div.get(mrap.fakeSeerCOAgent.get(LW)).get(HW).getResult() == Species.WEREWOLF){
				voteAllCandidates.remove(mrap.div.get(mrap.fakeSeerCOAgent.get(LW)).get(HW).getTarget());
				oneblackAgent.add(mrap.div.get(mrap.fakeSeerCOAgent.get(LW)).get(HW).getTarget());
			}
			else if(voteAllCandidates.contains(mrap.div.get(mrap.fakeSeerCOAgent.get(LW)).get(HW).getTarget()) && mrap.div.get(mrap.fakeSeerCOAgent.get(LW)).get(HW).getResult() == Species.HUMAN){
				voteAllCandidates.remove(mrap.div.get(mrap.fakeSeerCOAgent.get(LW)).get(HW).getTarget());
				onewhiteAgent.add(mrap.div.get(mrap.fakeSeerCOAgent.get(LW)).get(HW).getTarget());
			}
			}
			}//偽占いの占い先を除外
		for(LW = 0; LW < mrap.SeerCOAgent.size(); LW++){
			for(HW = 0; HW < mrap.div.get(mrap.SeerCOAgent.get(LW)).size();HW++){
			if(voteAllCandidates.contains(mrap.div.get(mrap.SeerCOAgent.get(LW)).get(HW).getTarget()) && mrap.div.get(mrap.SeerCOAgent.get(LW)).get(HW).getResult() == Species.WEREWOLF){
				voteAllCandidates.remove(mrap.div.get(mrap.SeerCOAgent.get(LW)).get(HW).getTarget());
				oneblackAgent.add(mrap.div.get(mrap.SeerCOAgent.get(LW)).get(HW).getTarget());
			}
			if(voteAllCandidates.contains(mrap.div.get(mrap.SeerCOAgent.get(LW)).get(HW).getTarget()) && mrap.div.get(mrap.SeerCOAgent.get(LW)).get(HW).getResult() == Species.HUMAN){
				voteAllCandidates.remove(mrap.div.get(mrap.SeerCOAgent.get(LW)).get(HW).getTarget());
				onewhiteAgent.add(mrap.div.get(mrap.SeerCOAgent.get(LW)).get(HW).getTarget());
			}
			}
			}//グレー占いの占い先を除外
		
		System.out.printf("投票先信占から狼:%s\n", blackAgent);
		System.out.printf("投票先誰かから狼:%s\n", oneblackAgent);
		System.out.printf("投票先霊能偽グレー:%s\n", fakeMEDIUMvoteAgent);
		System.out.printf("投票先信占からグレー、役職持ち除く:%s\n", voteCandidates);
		System.out.printf("投票先全員からグレー、役職持ち除く:%s\n", voteAllCandidates);
		System.out.printf("投票先占いグレー:%s\n", SeervoteAgent);
		System.out.printf("投票先偽占い:%s\n", fakeSeerCOvoteAgent);
		System.out.printf("投票先占い白:%s\n", whiteSeerAgent);
		System.out.printf("投票先霊能偽白:%s\n", whiteMEDIUMAgent);
		System.out.printf("投票先誰かからグレー白:%s\n", onewhiteAgent);
		System.out.printf("投票先信占からグレー白:%s\n", whiteAgent);
		double RAN1 = Math.random() * 4;
			if(mrap.isVote == true){
			return mrap.Voteagt;
			}
			else if(mrap.DetectWP < 4){
			if(blackAgent.size() > 0){//人狼を見つけていれば人狼に
				return randomSelect(blackAgent);}
			else if(fakeSeerCOvoteAgent.size() > 0 && RAN1 < mrap.nowday){//偽の占い師がいれば偽の占い師に
				return randomSelect(fakeSeerCOvoteAgent);}
			else if(voteAllCandidates.size() > 0 && mrap.nowday < 4){
				Agent agt1 = voteAllCandidates.get(0);
				if(mrap.nowday == 1){
					return randomSelect(voteAllCandidates);}
				else if(mrap.nowday == 2){
					int talkN = 5;
					for(i = 0; i < voteAllCandidates.size();i++){
						if(talkN > mrap.DayTalkNum[mrap.nowday][voteAllCandidates.get(i).getAgentIdx()]){
							talkN = mrap.DayTalkNum[mrap.nowday][voteAllCandidates.get(i).getAgentIdx()];
							agt1 = voteAllCandidates.get(i);
						}
						
					}
					if(Math.random() > 0.5){
						return agt1;
					}
					else{
						return randomSelect(voteAllCandidates);
					}
				}//俗に言う寡黙吊り
				else{
					double sum1 = 0;
					for(i = 0;i < voteAllCandidates.size(); i++){
						if(sum1 < (VoteJob[voteAllCandidates.get(i).getAgentIdx()][4] + VoteJob[voteAllCandidates.get(i).getAgentIdx()][5])){
							sum1 = VoteJob[voteAllCandidates.get(i).getAgentIdx()][4] + VoteJob[voteAllCandidates.get(i).getAgentIdx()][5];
							agt1 = voteAllCandidates.get(i);
						}}//グレーの中で一番怪しいと思っている人物を探す
					if(sum1 > Math.random()){
						return agt1;}
					else{
						return randomSelect(voteAllCandidates);}}}
			else if(voteCandidates.size() > 0){//４日目以降は信頼できる占い師の占い結果のみを使用
				Agent agt1 = voteCandidates.get(0);
					double sum1 = 0;
					for(i = 0;i < voteCandidates.size(); i++){
						if(sum1 < (VoteJob[voteCandidates.get(i).getAgentIdx()][4] + VoteJob[voteCandidates.get(i).getAgentIdx()][5])){
							sum1 = VoteJob[voteCandidates.get(i).getAgentIdx()][4] + VoteJob[voteCandidates.get(i).getAgentIdx()][5];
							agt1 = voteCandidates.get(i);
						}}//グレーの中で一番怪しいと思っている人物を探す
					if(sum1 > Math.random()){
						return agt1;}
					else{
						return randomSelect(voteCandidates);}}
			else if(SeervoteAgent.size() > 0){
				return randomSelect(SeervoteAgent);}//占いグレー
			else if(fakeSeerCOvoteAgent.size() > 0){
				return randomSelect(fakeSeerCOvoteAgent);}//偽占い
			else if(whiteSeerAgent.size() > 0){
				return randomSelect(whiteSeerAgent);}//占い白            
			else if(whiteMEDIUMAgent.size() > 0){
				return randomSelect(whiteMEDIUMAgent);}//霊能白
			else{
				return randomSelect(whiteAgent);}}
		else{//人外を4人見つけていれば
			if(blackAgent.size() > 0){//人狼を見つけていれば人狼に
				return randomSelect(blackAgent);}
			else if(fakeMEDIUMvoteAgent.size() > 0){//偽の霊能者がいれば偽の霊能者に
			    return randomSelect(fakeMEDIUMvoteAgent);}
			else if(SeervoteAgent.size() > 0){//占いのグレー
				return randomSelect(SeervoteAgent);}
			else if(fakeSeerCOvoteAgent.size() > 0){//偽占いのグレー
				return randomSelect(fakeSeerCOvoteAgent);}
			else{
				return getMe();
			}
		}
	}
	
	/**
	 *���Agent�̃��X�g���烉���_����Agent��I������
	 * @param agentList
	 * @return
	 */
	private Agent randomSelect(List<Agent> agentList){
		int num = new Random().nextInt(agentList.size());
		return agentList.get(num);
	}
	
	
	

}
