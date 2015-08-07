package org.aiwolf.myAgent;

import org.aiwolf.client.base.player.AbstractSeer;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.data.Status;

import java.util.*;

public class MySeer extends AbstractSeer {

	MyRoleAssignPlayer mrap;
	int i,k,s;
	
	
	
	public MySeer(MyRoleAssignPlayer mrap) {
		super();
		this.mrap = mrap;
		
	}
	

	
	//List<List<Utterance>> mrap.inq = new ArrayList<List<Utterance>>();
	
	// if (mrap.inq.get(a) == null) {
	//   mrap.inq.put(a, new ArrayList<Utterance));  // initialize
	// }
	// List<Utterance> uttlist = mrap.inq.get(a);
	// uttlist.add(hatsugen);
	
	// for (Utterance u : uttlist) {
	//   
	// }

	
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
		mrap.SEERDetectNum = 1;
		
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
			mrap.MyThinkJob[i][1] = 0;
			mrap.MyThinkJob[i][2] = 1.0 / (mrap.MaxNum - 1.0);
			mrap.MyThinkJob[i][3] = 1.0 / (mrap.MaxNum - 1.0);
			mrap.MyThinkJob[i][4] = 3.0 / (mrap.MaxNum - 1.0);
			mrap.MyThinkJob[i][5] = 1.0 / (mrap.MaxNum - 1.0);
			
			}
			else{
				mrap.MyThinkJob[i][0] = 0;
				mrap.MyThinkJob[i][1] = 1.0;
				mrap.MyThinkJob[i][2] = 0;
				mrap.MyThinkJob[i][3] = 0;
				mrap.MyThinkJob[i][4] = 0;
				mrap.MyThinkJob[i][5] = 0;
			}
			}
		for(i=0;i < 16;i++){
			for(k= 0;k < 6;k++){
				mrap.ThinkMEDIUMJob[i][k] = mrap.MyThinkJob[i][k]; 
			}}
		for(i=0;i < 16;i++){
			for(k= 0;k < 16;k++){
				mrap.HaveTrust[i][k] = 1 / (1 + Math.exp(-mrap.logit[i][k]));//信頼の基準値を0.5とする
			}}
		
		
		output();
	}
	
	@Override
	public Agent divine() {
		List<Agent> divineCandidates = new ArrayList<Agent>();//自分で占ってない生きている人リスト
		List<Agent> divineGrayCandidates = new ArrayList<Agent>();//誰も占ってない生きているリスト
		divineCandidates.addAll(getLatestDayGameInfo().getAliveAgentList());
		divineCandidates.remove(getMe());
		for(Judge judge: getMyJudgeList()){
		if(divineCandidates.contains(judge.getTarget())){
			divineCandidates.remove(judge.getTarget());
		}}
		divineGrayCandidates.addAll(divineCandidates);//霊能占い師除く完グレを選択
		for(i = 0;i < mrap.MEDIUMCOAgent.size(); i++){
			if(divineGrayCandidates.contains(mrap.MEDIUMCOAgent.get(i))){
				divineGrayCandidates.remove(mrap.MEDIUMCOAgent.get(i));
				divineCandidates.remove(mrap.MEDIUMCOAgent.get(i));
			}
		}
		for(i = 0;i < mrap.fakeMEDIUMAgent.size(); i++){
			if(divineGrayCandidates.contains(mrap.fakeMEDIUMAgent.get(i))){
				divineGrayCandidates.remove(mrap.fakeMEDIUMAgent.get(i));
				divineCandidates.remove(mrap.fakeMEDIUMAgent.get(i));
			}
		}
		for(i = 0;i < mrap.fakeSeerCOAgent.size();i++){
			if(divineGrayCandidates.contains(mrap.fakeSeerCOAgent.get(i))){
				divineGrayCandidates.remove(mrap.fakeSeerCOAgent.get(i));
				divineCandidates.remove(mrap.fakeSeerCOAgent.get(i));}
			for(k = 0;k < mrap.div.get(mrap.fakeSeerCOAgent.get(i)).size();k++){//他の占い師が占った先も除く
				if(divineGrayCandidates.contains(mrap.div.get(mrap.fakeSeerCOAgent.get(i)).get(k).getTarget())){
					divineGrayCandidates.remove(mrap.div.get(mrap.fakeSeerCOAgent.get(i)).get(k).getTarget());
				}
			}
		}
		
		double ran = ((mrap.gameInfo1.getDay() - 1) * 0.4) + 0.2;
		if(ran > 1.0){
			ran = 1.0;}
		if(mrap.nowday < 3){
		if(divineGrayCandidates.size() > 0){//完グレ選択
			int talkNum = 0;
			Agent Grayagt = divineGrayCandidates.get(0);
			for(i = 0; i < divineGrayCandidates.size();i++){//俗にいう多弁占い
				if(talkNum < mrap.DayTalkNum[mrap.nowday][divineGrayCandidates.get(i).getAgentIdx()]){
					talkNum = mrap.DayTalkNum[mrap.nowday][divineGrayCandidates.get(i).getAgentIdx()];
				Grayagt = divineGrayCandidates.get(i);
				}}
			if(talkNum > 0 && ran > Math.random()){
				return Grayagt;
			}
			return randomSelect(divineGrayCandidates);}
	       else{
			return getMe();//なければ自分
		}}
		else if(mrap.nowday >= 3){
			if(divineGrayCandidates.size() > 0){//完グレ選択
				double MJ = 0.0;
				Agent Grayagt = divineGrayCandidates.get(0);
				for(i = 0; i < divineGrayCandidates.size();i++){//俗にいう多弁占い
					if(MJ < mrap.MyThinkJob[divineGrayCandidates.get(i).getAgentIdx()][4]){
						MJ =  mrap.MyThinkJob[divineGrayCandidates.get(i).getAgentIdx()][4];
					Grayagt = divineGrayCandidates.get(i);
					}}
				
				return Grayagt;}//一番怪しいと思っている相手を指定
			else if(divineCandidates.size() > 0){//自分からのグレー選択
				double MJ = 0.0;
				Agent Grayagt = divineCandidates.get(0);
				for(i = 0; i < divineCandidates.size();i++){//俗にいう多弁占い
					if(MJ < mrap.MyThinkJob[divineCandidates.get(i).getAgentIdx()][4]){
						MJ =  mrap.MyThinkJob[divineCandidates.get(i).getAgentIdx()][4];
					Grayagt = divineCandidates.get(i);
					}}
				
				return Grayagt;
			}else{
				return getMe();//なければ自分
			}
		}
		else{
			return getMe();
		}
	}
	
	
	
	
	public void output(){//現状を表示，
		double sum;
		double sum1[] = new double[16];;
		if(mrap.MediNum != 0){
			System.out.printf("ThinkMEDIUMJob:from:Agent%d\n",mrap.MediNum);
			for(i = 1; i <= mrap.MaxNum; i++){
				System.out.printf("AGT%2d|",i);}
			System.out.printf("\n");
			for(i = 0; i < 6; i++){
				sum = 0;
				for(k = 1; k < 16; k++){
			System.out.printf("%.3f|",mrap.ThinkMEDIUMJob[k][i]);
			sum = sum + mrap.ThinkMEDIUMJob[k][i];
			sum1[k] = sum1[k] + mrap.ThinkMEDIUMJob[k][i]; 
				}
				System.out.printf("合計%.3f", sum);
				System.out.println("\n");}
			for(k = 1; k <= mrap.MaxNum; k++){
				System.out.printf("%.3f|",sum1[k]);}
			System.out.printf("\n");	
		}
		else if(mrap.MediNum == 0){
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
		System.out.printf("偽占いCOリスト");
		System.out.println(mrap.fakeSeerCOAgent);
		System.out.printf("生きている偽占いCOリスト");
		System.out.println(mrap.AlivefakeSeerCOAgent);
		System.out.printf("霊能COリスト");
		System.out.println(mrap.MEDIUMCOAgent);
		System.out.printf("生きている霊能COリスト");
		System.out.println(mrap.AliveMEDIUMCOAgent);
		for(i = 0; i < mrap.MEDIUMCOAgent.size(); i++){
			System.out.printf("%s:%d\n", mrap.MEDIUMCOAgent.get(i),mrap.DetectWPO[mrap.MEDIUMCOAgent.get(i).getAgentIdx()]);
		}
		System.out.printf("偽霊能COリスト");
		System.out.println(mrap.fakeMEDIUMAgent);
		System.out.printf("生きている偽霊能COリスト");
		System.out.println(mrap.AlivefakeMEDIUMAgent);
		for(i = 0; i < mrap.fakeMEDIUMAgent.size(); i++){
			System.out.printf("%s:%d\n", mrap.fakeMEDIUMAgent.get(i),mrap.DetectWPO[mrap.fakeMEDIUMAgent.get(i).getAgentIdx()]);
		}
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
	public void finish() {//最後に得られる情報も表示
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
			System.out.printf("偽占いCOリスト");
			System.out.println(mrap.fakeSeerCOAgent);
			System.out.printf("生きている偽占いCOリスト");
			System.out.println(mrap.AlivefakeSeerCOAgent);
			System.out.printf("霊能COリスト");
			System.out.println(mrap.MEDIUMCOAgent);
			System.out.printf("生きている霊能COリスト");
			System.out.println(mrap.AliveMEDIUMCOAgent);
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
	
    public void MeUp(Agent agt){//引数の霊能者の情報をmrap.ThinkMEDIUMJobに反映させる
    	int MeI;
    	int Mk;
    	for(MeI = 0; MeI < mrap.inq.get(agt).size();MeI++){
    		if(!mrap.judgeAgentList.contains(mrap.inq.get(agt).get(MeI).getTarget())){
    	if(mrap.inq.get(agt).get(MeI).getResult() == Species.HUMAN){
    	mrap.ThinkMEDIUMJob[mrap.inq.get(agt).get(MeI).getTarget().getAgentIdx()][4] = 0.0;}
    	else if(mrap.inq.get(agt).get(MeI).getResult() == Species.WEREWOLF){
    		if(!mrap.fakeSeerCOAgent.contains(mrap.inq.get(agt).get(MeI)) && !mrap.MEDIUMCOAgent.contains(mrap.inq.get(agt).get(MeI)) && !mrap.fakeMEDIUMAgent.contains(mrap.inq.get(agt).get(MeI))){
    			mrap.MeDetectWP++;
    		}
    		for(Mk = 0; Mk < 6; Mk++){
    		mrap.ThinkMEDIUMJob[mrap.inq.get(agt).get(MeI).getTarget().getAgentIdx()][Mk] = 0.0;
    		}
    		mrap.ThinkMEDIUMJob[mrap.inq.get(agt).get(MeI).getTarget().getAgentIdx()][4] = 1.0;
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
			mrap.CHT(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),mrap.vot.get(mrap.nowday - 1).get(i).getTarget().getAgentIdx(),-mrap.WV);}
			if(mrap.vot.get(mrap.nowday - 1).get(i).getTarget() == getMe() && mrap.isComingOut == true){//カミングアウトした自分に投票する相手の人外値を上昇
				mrap.UJP(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),Role.WEREWOLF,mrap.WV);
				mrap.UJP(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),Role.POSSESSED,mrap.WV);}
			else if(mrap.judgeAgentList.contains(mrap.vot.get(mrap.nowday - 1).get(i).getTarget()) && mrap.isComingOut == true){//投票が自分の占い先でかつ、自分が白を出していた時に
				if(mrap.judgeList.get(mrap.judgeAgentList.indexOf(mrap.vot.get(mrap.nowday - 1).get(i).getTarget())).getResult() == Species.HUMAN){
					mrap.CHT(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),getMe().getAgentIdx(),-mrap.WV);
					mrap.UJP(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),Role.WEREWOLF,0.2);
					mrap.UJP(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),Role.POSSESSED,0.2);
				}
				else if(mrap.judgeList.get(mrap.judgeAgentList.indexOf(mrap.vot.get(mrap.nowday - 1).get(i).getTarget())).getResult() == Species.WEREWOLF){//投票が自分の占い先でかつ、自分が黒をだしている
					mrap.CHT(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),getMe().getAgentIdx(),mrap.WV);
					mrap.DJP(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
					mrap.DJP(mrap.vot.get(mrap.nowday - 1).get(i).getAgent().getAgentIdx(),Role.POSSESSED,0.1);
				}}
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
		for(i=0;i < 16;i++){
			for(k= 0;k < 16;k++){
			mrap.prelogit[i][k] = mrap.logit[i][k];//前日までの信頼度をここに保存しておく 
			}}
		if(mrap.MyState == 0){
			//commonThink(mrap.nowday);
			//think(mrap.nowday);
			if(mrap.nowday >= 1){
				mrap.judgeList.add(mrap.gameInfo1.getDivineResult());
				mrap.judgeAgentList.add(mrap.gameInfo1.getDivineResult().getTarget());
				System.out.printf("日にち%d\n", mrap.nowday);
			if(mrap.gameInfo1.getDivineResult().getResult() == Species.HUMAN){
				System.out.printf("人間発見\n");
				mrap.WhiteAgent.add(mrap.gameInfo1.getDivineResult().getTarget());
						mrap.MyThinkJob[mrap.gameInfo1.getDivineResult().getTarget().getAgentIdx()][4] = 0.0;
						System.out.printf("番号%d\n", mrap.gameInfo1.getDivineResult().getTarget().getAgentIdx());
						mrap.CleanJob(mrap.gameInfo1.getDivineResult().getTarget().getAgentIdx());}
			else if(mrap.gameInfo1.getDivineResult().getResult() == Species.WEREWOLF){
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
				mrap.BlackAgent.add(mrap.gameInfo1.getDivineResult().getTarget());
				mrap.WEREWOLFAgent.add(mrap.gameInfo1.getDivineResult().getTarget());
				mrap.AliveWEREWOLFAgent.add(mrap.gameInfo1.getDivineResult().getTarget());
				
				if(mrap.gameInfo1.getDivineResult().getResult() == Species.WEREWOLF && !mrap.DIVWEREAgent.contains(mrap.gameInfo1.getDivineResult().getTarget())){
					mrap.DIVWEREAgent.add(mrap.gameInfo1.getDivineResult().getTarget());
				}
				if(mrap.fakeMEDIUMAgent.contains(mrap.gameInfo1.getDivineResult().getTarget())){
					
				}
				else if(mrap.MEDIUMCOAgent.contains(mrap.gameInfo1.getDivineResult().getTarget()) && !mrap.fakeMEDIUMAgent.contains(mrap.gameInfo1.getDivineResult().getTarget())){
					mrap.MEDIUMCOAgent.remove(mrap.gameInfo1.getDivineResult().getTarget());
					mrap.AliveMEDIUMCOAgent.remove(mrap.gameInfo1.getDivineResult().getTarget());
					mrap.fakeMEDIUMAgent.add(mrap.gameInfo1.getDivineResult().getTarget());
					mrap.AlivefakeMEDIUMAgent.add(mrap.gameInfo1.getDivineResult().getTarget());
					if(mrap.MEDIUMCOAgent.size() + mrap.fakeMEDIUMAgent.size() == 1){
						mrap.DetectWP++;}
				}
				else{
					mrap.DetectWP++;
				}
				for(i = 0;i <= 5; i++){
						mrap.MyThinkJob[mrap.gameInfo1.getDivineResult().getTarget().getAgentIdx()][i] = 0;}
				mrap.MyThinkJob[mrap.gameInfo1.getDivineResult().getTarget().getAgentIdx()][4] = 1.0;
				mrap.WEREWOLFDetectNum++;}
			mrap.ChangeJob(mrap.gameInfo1.getDivineResult().getTarget().getAgentIdx(),100,100,100,100);
			
		}
			
		}
		think(mrap.nowday);
		output();
	}
	

	public void MEDIUMCO1(){//霊能者が二人以上でたら呼び出す，それぞれからそれぞれへの信頼度を変更
		for(int M1 = 0; M1 < mrap.fakeMEDIUMAgent.size();M1++){
			TON[DEN] = mrap.fakeMEDIUMAgent.get(M1).getAgentIdx();
			DEN++;
			for(int M2 = 0; M2 < mrap.fakeMEDIUMAgent.size();M2++){
				if(M1 != M2){
					mrap.HaveTrust[mrap.fakeMEDIUMAgent.get(M1).getAgentIdx()][mrap.fakeMEDIUMAgent.get(M2).getAgentIdx()] = 0.0;
				}}
			for(int M2 = 0; M2 < mrap.MEDIUMCOAgent.size();M2++){
				mrap.HaveTrust[mrap.fakeMEDIUMAgent.get(M1).getAgentIdx()][mrap.MEDIUMCOAgent.get(M2).getAgentIdx()] = 0.0;
			}
			}
		for(int M1 = 0; M1 < mrap.MEDIUMCOAgent.size();M1++){
			TON[DEN] = mrap.MEDIUMCOAgent.get(M1).getAgentIdx();
			DEN++;
			for(int M2 = 0; M2 < mrap.MEDIUMCOAgent.size();M2++){
				if(M1 != M2){
					mrap.HaveTrust[mrap.MEDIUMCOAgent.get(M1).getAgentIdx()][mrap.MEDIUMCOAgent.get(M2).getAgentIdx()] = 0.0;
				}}
			for(int M2 = 0; M2 < mrap.fakeMEDIUMAgent.size();M2++){
				mrap.HaveTrust[mrap.MEDIUMCOAgent.get(M1).getAgentIdx()][mrap.fakeMEDIUMAgent.get(M2).getAgentIdx()] = 0.0;
			}
			}
		
	}
	int TON[] = new int[4];
	int DEN = 0;
	void MySeerUPD(Talk talk, Utterance utterance){//updateから呼び出す，他の人物の発言を処理
		int INQTF;//霊能結果が合致してたかどうかの確認
		int TAN;
		DEN = 0;
		if(!talk.getAgent().equals(getMe())){
			switch (utterance.getTopic()){
			case COMINGOUT:
				TAN = utterance.getTarget().getAgentIdx(); //TalkAgentNum
				if(utterance.getRole() == Role.SEER && !mrap.fakeSeerCOAgent.contains(talk.getAgent())){
					if(mrap.div.get(utterance.getTarget()) == null){
						mrap.div.put(utterance.getTarget(), new ArrayList<Utterance>());
					}
					RestartUPD(TAN, Role.SEER);
					if(mrap.FirstSeerCODay == 100){
						mrap.FirstSeerCODay = mrap.nowday;}
					mrap.fakeSeerCOAgent.add(utterance.getTarget());
					mrap.AlivefakeSeerCOAgent.add(utterance.getTarget());
					if(mrap.COAgent.contains(utterance.getTarget())){//すでに他でCOしている人物がCOしてきたら，人外度を上昇させる
						mrap.UJP(TAN,Role.POSSESSED,0.4);
						mrap.UJP(TAN,Role.WEREWOLF,0.4);
					}
					else{//他でCOしてなくてまだリストがなければ作成
					mrap.COAgent.add(utterance.getTarget());}
					if(mrap.div.get(utterance.getTarget()) == null){
						mrap.div.put(utterance.getTarget(), new ArrayList<Utterance>());
					}
					if(mrap.fakeSeerCOAgent.size() == 1){//占いCO一人目なら
						mrap.DetectWP++;
						mrap.HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
						mrap.PCJ(TAN,Role.VILLAGER,0.0);
						mrap.PCJ(TAN,Role.SEER,0.0);
						mrap.PCJ(TAN,Role.MEDIUM,0.0);
						mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
					mrap.ChangeJob(TAN,100,100,100,100);}
					else if(mrap.fakeSeerCOAgent.size() == 2){
						mrap.DetectWP++;
						mrap.HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
						for(int M1 = 0; M1 < mrap.fakeSeerCOAgent.size();M1++){
							for(int M2 = 0; M2 < mrap.fakeSeerCOAgent.size();M2++){
								if(M1 != M2){
									mrap.HaveTrust[mrap.fakeSeerCOAgent.get(M1).getAgentIdx()][mrap.fakeSeerCOAgent.get(M2).getAgentIdx()] = 0.0;
								}}}
						mrap.PCJ(TAN,Role.VILLAGER,0.0);
						mrap.PCJ(TAN,Role.SEER,0.0);
						mrap.PCJ(TAN,Role.MEDIUM,0.0);
						mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
						mrap.ChangeJob(TAN,mrap.fakeSeerCOAgent.get(0).getAgentIdx(),100,100,100);}
					else if(mrap.fakeSeerCOAgent.size() == 3){
						mrap.DetectWP++;
						mrap.HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
						for(int M1 = 0; M1 < mrap.fakeSeerCOAgent.size();M1++){
							for(int M2 = 0; M2 < mrap.fakeSeerCOAgent.size();M2++){
								if(M1 != M2){
									mrap.HaveTrust[mrap.fakeSeerCOAgent.get(M1).getAgentIdx()][mrap.fakeSeerCOAgent.get(M2).getAgentIdx()] = 0.0;
								}}}
						mrap.PCJ(TAN,Role.VILLAGER,0.0);
						mrap.PCJ(TAN,Role.SEER,0.0);
						mrap.PCJ(TAN,Role.MEDIUM,0.0);
						mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
						mrap.ChangeJob(TAN,mrap.fakeSeerCOAgent.get(0).getAgentIdx(),mrap.fakeSeerCOAgent.get(1).getAgentIdx(),100,100);}
					else if(mrap.fakeSeerCOAgent.size() == 4){
						mrap.DetectWP++;
						mrap.HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
						for(int M1 = 0; M1 < mrap.fakeSeerCOAgent.size();M1++){
							for(int M2 = 0; M2 < mrap.fakeSeerCOAgent.size();M2++){
								if(M1 != M2){
									mrap.HaveTrust[mrap.fakeSeerCOAgent.get(M1).getAgentIdx()][mrap.fakeSeerCOAgent.get(M2).getAgentIdx()] = 0.0;
								}}}
						mrap.PCJ(TAN,Role.VILLAGER,0.0);
						mrap.PCJ(TAN,Role.SEER,0.0);
						mrap.PCJ(TAN,Role.MEDIUM,0.0);
						mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
						mrap.ChangeJob(TAN,mrap.fakeSeerCOAgent.get(0).getAgentIdx(),mrap.fakeSeerCOAgent.get(1).getAgentIdx(),mrap.fakeSeerCOAgent.get(2).getAgentIdx(),100);}
					if(mrap.WEREWOLFAgent.contains(talk.getAgent())){
						mrap.DetectWP--;//先に占って狼だとわかっていたら値をマイナス１して調整
					}
					if(mrap.fakeSeerCOAgent.size() >= 1){
						int MC, MD;
						for(MC = 1; MC <= mrap.MaxNum; MC++){
							mrap.DetectWPO[MC]++;//二人以上占い師（自分含める）がいれば一人以外は偽物だと全員がわかる
							if(mrap.fakeSeerCOAgent.contains(Agent.getAgent(MC))){//占い師で
								for(MD = 0;MD < mrap.div.get(Agent.getAgent(MC)).size();MD++){
									if(mrap.div.get(Agent.getAgent(MC)).get(MD).getTarget() == utterance.getTarget() && mrap.div.get(Agent.getAgent(MC)).get(MD).getResult() == Species.WEREWOLF){
							mrap.DetectWPO[MC]--;//占い師が先に占っていて狼だったらマイナス１して調整
									}}}
							
							}
						
					}
				}//偽占い師が出たらその職業値を人狼と狂人以外０にする
				if(utterance.getRole() == Role.MEDIUM && !mrap.MEDIUMCOAgent.contains(talk.getAgent()) && !mrap.fakeMEDIUMAgent.contains(talk.getAgent())){
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
					
					
					mrap.MEDIUMCOAgent.add(utterance.getTarget());
					mrap.AliveMEDIUMCOAgent.add(utterance.getTarget());
					if(mrap.WEREWOLFAgent.contains(talk.getAgent())){//霊能COの前に人狼だと分かっていたら
						mrap.MEDIUMCOAgent.remove(utterance.getTarget());
						mrap.AliveMEDIUMCOAgent.remove(utterance.getTarget());
						mrap.fakeMEDIUMAgent.add(utterance.getTarget());
						mrap.AlivefakeMEDIUMAgent.add(utterance.getTarget());
						mrap.PCJ(TAN,Role.MEDIUM,0.0);}
				if(mrap.MEDIUMCOAgent.size() + mrap.fakeMEDIUMAgent.size() == 1){//霊能CO一人目なら
					mrap.PCJ(TAN,Role.VILLAGER,0.0);
					mrap.PCJ(TAN,Role.SEER,0.0);
					mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
					mrap.ChangeJob(TAN,100,100,100,100);}
				else if(mrap.MEDIUMCOAgent.size() + mrap.fakeMEDIUMAgent.size() == 2){//霊能CO二人目なら
					mrap.DetectWP++;
					MEDIUMCO1();
					mrap.PCJ(TAN,Role.VILLAGER,0.0);
					mrap.PCJ(TAN,Role.SEER,0.0);
					mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
					mrap.ChangeJob(TAN,TON[0],100,100,100);}
				else if(mrap.MEDIUMCOAgent.size() + mrap.fakeMEDIUMAgent.size() == 3){//霊能CO三人目なら
					mrap.DetectWP++;
					MEDIUMCO1();
					mrap.PCJ(TAN,Role.VILLAGER,0.0);
					mrap.PCJ(TAN,Role.SEER,0.0);
					mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
					mrap.ChangeJob(TAN,TON[0],TON[1],100,100);}
				else if(mrap.MEDIUMCOAgent.size() + mrap.fakeMEDIUMAgent.size() == 4){//霊能CO４人目なら
					mrap.DetectWP++;
					MEDIUMCO1();
					mrap.PCJ(TAN,Role.VILLAGER,0.0);
					mrap.PCJ(TAN,Role.SEER,0.0);
					mrap.PCJ(TAN,Role.BODYGUARD,0.0);
					mrap.CleanJob(TAN);
					mrap.ChangeJob(TAN,TON[0],TON[1],TON[2],100);}
					
					if(mrap.MEDIUMCOAgent.size() + mrap.fakeMEDIUMAgent.size() >= 2){
						int MC, MD;
						for(MC = 1; MC <= mrap.MaxNum; MC++){
							mrap.DetectWPO[MC]++;//二人以上霊能者がいれば一人以外は偽物だと全員がわかる
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
									mrap.HaveTrust[mrap.BODYGUARDCOAgent.get(M1).getAgentIdx()][mrap.BODYGUARDCOAgent.get(M2).getAgentIdx()] = 0.0;
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
				if(!mrap.fakeSeerCOAgent.contains(talk.getAgent())){//まだCOしてないのに占い結果報告なんてしてきたら
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.1);
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
				}//人外の値上昇
				mrap.adddiv(talk.getAgent(), utterance);//結果格納
				RestartUPD(talk.getAgent().getAgentIdx(), Role.SEER);
				break;
			case INQUESTED://霊能結果報告
				if(!mrap.fakeMEDIUMAgent.contains(talk.getAgent()) && !mrap.MEDIUMCOAgent.contains(talk.getAgent())){//まだCOしてないのに霊能結果報告なんてしてきたら
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.1);
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
				}//人外の値上昇
				TAN = utterance.getTarget().getAgentIdx(); 
				INQTF = mrap.Seeraddinq(talk.getAgent(),utterance);//結果格納
				if(INQTF == 1 && !mrap.fakeMEDIUMAgent.contains(talk.getAgent()) && mrap.MEDIUMCOAgent.contains(talk.getAgent())){//まだ偽だと分かっていない人の霊能結果が食い違っていた場合
					mrap.BlackAgent.add(talk.getAgent());
					mrap.MEDIUMCOAgent.remove(talk.getAgent());
					mrap.AliveMEDIUMCOAgent.remove(talk.getAgent());
					mrap.fakeMEDIUMAgent.add(talk.getAgent());
					mrap.AlivefakeMEDIUMAgent.add(talk.getAgent());
					if(mrap.MEDIUMCOAgent.size() == 0){
					mrap.DetectWP++;	
					}
					}
				RestartUPD(talk.getAgent().getAgentIdx(), Role.MEDIUM);
				break;
				
			case ESTIMATE://あまり使ってない，信頼度増減
				mrap.DayTalkNum[mrap.nowday][talk.getAgent().getAgentIdx()]++;
				if(utterance.getRole() == Role.WEREWOLF || utterance.getRole() == Role.POSSESSED){
					mrap.CHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),-mrap.TWE);}
				else{
					mrap.CHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),mrap.TWE);
				}
				break;
			case GUARDED://ガード発言だが，あまり考慮してない
				if(!mrap.BODYGUARDCOAgent.contains(talk.getAgent())){//まだCOしてないのに霊能結果報告なんてしてきたら
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.1);
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
				}//人外の値上昇
				if(mrap.BGA > 0){
					mrap.UJP(talk.getAgent().getAgentIdx(),Role.BODYGUARD,(mrap.BGA / 2.0) * mrap.TWG);
				}
				break;
			case VOTE://vote発言を処理
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
				else if(mrap.judgeAgentList.contains(utterance.getTarget()) && mrap.isComingOut == true){//投票が自分の占い先でかつ、自分が白を出していた時に
					if(mrap.judgeList.get(mrap.judgeAgentList.indexOf(utterance.getTarget())).getResult() == Species.HUMAN){
						mrap.CHT(talk.getAgent().getAgentIdx(),getMe().getAgentIdx(),-mrap.TWV);
						mrap.UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.2);
						mrap.UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.2);
					}
					if(mrap.judgeList.get(mrap.judgeAgentList.indexOf(utterance.getTarget())).getResult() == Species.WEREWOLF){//投票が自分の占い先でかつ、自分が黒をだしている
						mrap.CHT(talk.getAgent().getAgentIdx(),getMe().getAgentIdx(),mrap.TWV);
						mrap.DJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
						mrap.DJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.1);
					}}//他の占い師などの事はその占い師の場所で処理
				
				
				break;
			case AGREE://良く分からない
				mrap.DayTalkNum[mrap.nowday][talk.getAgent().getAgentIdx()]++;
				mrap.CHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),mrap.TWA);
				break;
			case DISAGREE://同様
				mrap.DayTalkNum[mrap.nowday][talk.getAgent().getAgentIdx()]++;
				mrap.CHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),mrap.TWDA);
				break;
			case SKIP://OVERと違いがないので，なくしてもよい
				mrap.DJP(talk.getAgent().getAgentIdx(), Role.VILLAGER, 0.05);
				break;
				default:
					break;
			}}
	}
	
	
	void RestartUPD(int Rn, Role rerole){//他のエージェントが役職持ちだと分かった時に役職持ちの状態でその日の最初から，（占い師だと分かってたり占い結果がある状態だと相手の考え方が変わるため）
		int RS;
		for(RS = 1; RS <= 15; RS++){
		mrap.HaveTrust[Rn][RS] = 1 / (1 + Math.exp(-mrap.prelogit[Rn][RS]));//評価を当日の最初まで巻き戻す
		}
		for(RS = 0; RS < mrap.daytalkList.get(mrap.nowday).size() - 1; RS++){
			Utterance utterance = new Utterance(mrap.daytalkList.get(mrap.nowday).get(RS).getContent());
		switch(rerole){//やり直すのは占い師と霊能者だけ
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
		for(RS = 0; RS < mrap.fakeSeerCOAgent.size(); RS++){
			mrap.HaveTrust[Rn][mrap.fakeSeerCOAgent.get(RS).getAgentIdx()] = 0;
		}
		break;
		case MEDIUM:
			for(RS = 0; RS < mrap.fakeMEDIUMAgent.size(); RS++){
				mrap.HaveTrust[Rn][mrap.fakeMEDIUMAgent.get(RS).getAgentIdx()] = 0;
			}	
			for(RS = 0; RS < mrap.MEDIUMCOAgent.size(); RS++){
				mrap.HaveTrust[Rn][mrap.MEDIUMCOAgent.get(RS).getAgentIdx()] = 0;
			}
		}
	}
	
	
	
	

	
	
	@Override
	public void update(GameInfo gameInfo) {//
		super.update(gameInfo);
		mrap.gameInfo1 = gameInfo;
		mrap.MediNum = 0;
		mrap.MeDetectWP = mrap.DetectWP;//発見した数を自分で見つけた数に初期化
		List<Talk> talkList = gameInfo.getTalkList();//今日の会話を記録する
		//System.out.printf("alltalksize%d\n",Allmrap.daytalkList.size());
		//List<Judge> judge1 = new ArrayList<Judge>();
		//Judge judge2 = mrap.gameInfo1.getDivineResult();
		//PP = 1;
		

		for(i=0;i < 16;i++){
			for(k= 0;k < 6;k++){
				mrap.ThinkMEDIUMJob[i][k] = mrap.MyThinkJob[i][k]; 
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
			
			
				MySeerUPD(talk,utterance);//最初に自分の処理を行う
			
			for(int M = 1; M < 16; M++){//自分以外の全員の処理を行う
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
		if(mrap.myrole != Role.MEDIUM){//信頼できる霊能者がいるか確認
		if(mrap.MEDIUMCOAgent.size() > 0){
		for(i = 0; i < mrap.MEDIUMCOAgent.size(); i++){
		if(mrap.BOM < mrap.MyThinkJob[mrap.MEDIUMCOAgent.get(i).getAgentIdx()][2]){//霊能者の信頼度が高ければ
			MeUp(mrap.MEDIUMCOAgent.get(i));
			mrap.MediNum = mrap.MEDIUMCOAgent.get(i).getAgentIdx();
		}
		}}
		}
	} //update 
	
	
	@Override
	public String talk() {
		String voteTalk;
		if(!mrap.isComingOut){
			for(Judge judge: getMyJudgeList()){
				if(judge.getResult() == Species.WEREWOLF || mrap.fakeSeerCOAgent.size() > 0 || mrap.nowday >= mrap.RAN){
					String comingoutTalk = TemplateTalkFactory.comingout(getMe(), getMyRole());
					mrap.isComingOut = true;
					return comingoutTalk;
				}}
		}
		else{
			for(Judge judge:getMyJudgeList()){
				if(!mrap.myToldJudgeList.contains(judge)){
					String resultTalk = TemplateTalkFactory.divined(judge.getTarget(), judge.getResult());
					mrap.myToldJudgeList.add(judge);
					return resultTalk;
				}}}
		if(mrap.AliveWEREWOLFAgent.size() > 0 && !mrap.isVote){//まだ生きている分かっている狼がいれば
			System.out.printf("とりあえず狼にいれよう\n");
			mrap.Voteagt = randomSelect(mrap.AliveWEREWOLFAgent);
			voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
			mrap.isVote = true;
			return voteTalk;
		}
		else if(mrap.DetectWP >= 4 && !mrap.isVote){//人外を４以上（普通なら４で止まる）見つけた時、ただ既に見つけた狼は処刑した状態なはず
			System.out.printf("人外全露出じゃないですか？\n");
			System.out.printf("%d:%s",mrap.DetectWP,mrap.isVote);
			if(mrap.fakeMEDIUMAgent.size() >= 2 && mrap.AlivefakeMEDIUMAgent.size() > 0){//偽霊能に投票
				mrap.isVote = true;
				mrap.Voteagt = randomSelect(mrap.AlivefakeMEDIUMAgent);
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
			else if(mrap.SeerCOAgent.size() + mrap.fakeSeerCOAgent.size() >= 2 && mrap.AliveSeerCOAgent.size() > 0){//占いグレーに投票
				mrap.isVote = true;
				mrap.Voteagt = randomSelect(mrap.AliveSeerCOAgent);
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
			else if(mrap.SeerCOAgent.size() + mrap.fakeSeerCOAgent.size()  >= 2 && mrap.AlivefakeSeerCOAgent.size() > 0){//偽占い師に投票
				mrap.Voteagt = randomSelect(mrap.AlivefakeSeerCOAgent);
				mrap.isVote = true;
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
			mrap.isVote = true;
		}
		else if(mrap.DetectWP <= 3 && !mrap.isVote && (mrap.MEDIUMCOAgent.size() + mrap.fakeMEDIUMAgent.size()) >= 2 && (mrap.AliveMEDIUMCOAgent.size() + mrap.AlivefakeMEDIUMAgent.size()) >= 1 && mrap.nowday >= 3){//霊ロラ始めまたは完遂
			System.out.printf("霊ロラしようぜ\n");
			if(mrap.AlivefakeMEDIUMAgent.size() > 0){
				mrap.isVote = true;
				mrap.Voteagt = randomSelect(mrap.AlivefakeMEDIUMAgent);
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
			else if(mrap.AliveMEDIUMCOAgent.size() > 0){
				mrap.Voteagt = randomSelect(mrap.AliveMEDIUMCOAgent);
				mrap.isVote = true;
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
		}
		else if(mrap.DetectWP <= 3 && !mrap.isVote && (mrap.MEDIUMCOAgent.size() + mrap.fakeMEDIUMAgent.size()) >= 3 && (mrap.AliveMEDIUMCOAgent.size() + mrap.AlivefakeMEDIUMAgent.size()) >= 1 && mrap.nowday >= 2){//霊ロラ始めまたは完遂
			System.out.printf("霊ロラしようぜ(霊3以上ver)\n");
			if(mrap.AlivefakeMEDIUMAgent.size() > 0){
				mrap.isVote = true;
				mrap.Voteagt = randomSelect(mrap.AlivefakeMEDIUMAgent);
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
			else if(mrap.AliveMEDIUMCOAgent.size() > 0){
				mrap.Voteagt = randomSelect(mrap.AliveMEDIUMCOAgent);
				mrap.isVote = true;
				voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
				return voteTalk;}
		}
		else if(mrap.DetectWP <= 3 && !mrap.isVote && mrap.fakeSeerCOAgent.size() >= 2 && mrap.AlivefakeSeerCOAgent.size() > 1 && mrap.nowday >= 3){//占いロラ
			System.out.printf("占いロラしようぜ\n");
			mrap.isVote = true;
			mrap.Voteagt = randomSelect(mrap.AlivefakeSeerCOAgent);
			voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
			return voteTalk;
		}
		else if(mrap.DetectWP <= 3 && !mrap.isVote && mrap.fakeSeerCOAgent.size() >= 3 && mrap.AlivefakeSeerCOAgent.size() > 1 && mrap.nowday >= 2){//占いロラ
			System.out.printf("占いロラしようぜ(占い4以上ver)\n");
			mrap.isVote = true;
			mrap.Voteagt = randomSelect(mrap.AlivefakeSeerCOAgent);
			voteTalk = TemplateTalkFactory.vote(mrap.Voteagt);
			return voteTalk;
		}
		
		return Talk.OVER;
	}

	@Override
	public Agent vote() {//投票は下記output表の上から順に優先
		//List<Agent> voteCandidates = new ArrayList<Agent>();
		double VoteJob[][] = new double[16][6];
		if(mrap.MediNum == 0){//現在信頼できそうな霊能者がいればその結果を反映、
		for(i=0;i < 16;i++){
			for(k= 0;k < 6;k++){
				VoteJob[i][k] = mrap.MyThinkJob[i][k]; 
			}}}
		else if(mrap.MediNum != 0){
			for(i=0;i < 16;i++){
				for(k= 0;k < 6;k++){
					VoteJob[i][k] = mrap.ThinkMEDIUMJob[i][k]; 
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
		List<Agent> MEDIUMvoteAgent = new ArrayList<Agent>();
		List<Agent> blackAgent = new ArrayList<Agent>();
		List<Agent> fakeSeerCOvoteAgent = new ArrayList<Agent>();
		List<Agent> fakeMEDIUMvoteAgent = new ArrayList<Agent>();
		List<Agent> whiteSeerAgent = new ArrayList<Agent>();
		List<Agent> whiteMEDIUMAgent = new ArrayList<Agent>();
		for(Judge judge: getMyJudgeList()){
if(getLatestDayGameInfo().getAliveAgentList().contains(judge.getTarget())){
    if(mrap.fakeSeerCOAgent.contains(judge.getTarget())){   
	switch (judge.getResult()){
        case HUMAN:
        	whiteSeerAgent.add(judge.getTarget());
        			break;
        case WEREWOLF:
        	blackAgent.add(judge.getTarget());
        	break;}}
    else if(mrap.MEDIUMCOAgent.contains(judge.getTarget())){
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

		}
		if(mrap.MEDIUMCOAgent.size() > 0){
			for(int i = 0; i < mrap.MEDIUMCOAgent.size(); i++){
			if(getLatestDayGameInfo().getAliveAgentList().contains(mrap.MEDIUMCOAgent.get(i)) && !blackAgent.contains(mrap.MEDIUMCOAgent.get(i)) && !whiteMEDIUMAgent.contains(mrap.MEDIUMCOAgent.get(i))){
				MEDIUMvoteAgent.add(mrap.MEDIUMCOAgent.get(i));
			}
			}}
		if(mrap.fakeMEDIUMAgent.size() > 0){
			for(int i =0; i < mrap.fakeMEDIUMAgent.size(); i++){
				if(getLatestDayGameInfo().getAliveAgentList().contains(mrap.fakeMEDIUMAgent.get(i)) && !blackAgent.contains(mrap.fakeMEDIUMAgent.get(i)) && !whiteMEDIUMAgent.contains(mrap.fakeMEDIUMAgent.get(i))){
					fakeMEDIUMvoteAgent.add(mrap.fakeMEDIUMAgent.get(i));
				}
			}
		}
		if(mrap.fakeSeerCOAgent.size() > 0){
			for(int i = 0; i < mrap.fakeSeerCOAgent.size(); i++){
			if(getLatestDayGameInfo().getAliveAgentList().contains(mrap.fakeSeerCOAgent.get(i)) && !blackAgent.contains(mrap.fakeSeerCOAgent.get(i)) && !whiteSeerAgent.contains(mrap.fakeSeerCOAgent.get(i))){
			fakeSeerCOvoteAgent.add(mrap.fakeSeerCOAgent.get(i));
			}}}
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
		if(MEDIUMvoteAgent.size() > 0){
		voteCandidates.removeAll(MEDIUMvoteAgent);}
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
			}
			}
			}
		
		
		System.out.printf("投票先狼:%s\n", blackAgent);
		System.out.printf("投票先霊能偽:%s\n", fakeMEDIUMvoteAgent);
		System.out.printf("投票先自分からグレー、役職持ち除く:%s\n", voteCandidates);
		System.out.printf("投票先全員からグレー、役職持ち除く:%s\n", voteAllCandidates);
		System.out.printf("投票先霊能グレー:%s\n", MEDIUMvoteAgent);
		System.out.printf("投票先占いグレー:%s\n", fakeSeerCOvoteAgent);
		System.out.printf("投票先占い白:%s\n", whiteSeerAgent);
		System.out.printf("投票先霊能白:%s\n", whiteMEDIUMAgent);
		System.out.printf("投票先自分からグレー白:%s\n", whiteAgent);
			if(mrap.isVote == true){
			return mrap.Voteagt;
			}
			else if(mrap.DetectWP < 4){
			if(blackAgent.size() > 0){//人狼を見つけていれば人狼に
				return randomSelect(blackAgent);}
			else if(fakeMEDIUMvoteAgent.size() > 0){//偽の霊能者がいれば偽の霊能者に
				return randomSelect(fakeMEDIUMvoteAgent);}
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
			else if(voteCandidates.size() > 0){//４日目以降は自分の占い結果のみを使用
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
			else if(MEDIUMvoteAgent.size() > 0){
				return randomSelect(MEDIUMvoteAgent);}
			else if(fakeSeerCOvoteAgent.size() > 0){
				return randomSelect(fakeSeerCOvoteAgent);}
			else if(whiteSeerAgent.size() > 0){
				return randomSelect(whiteSeerAgent);}            
			else if(whiteMEDIUMAgent.size() > 0){
				return randomSelect(whiteMEDIUMAgent);}
			else{
				return randomSelect(whiteAgent);}}
		else{//人外を4人見つけていれば
			if(blackAgent.size() > 0){//人狼を見つけていれば人狼に
				return randomSelect(blackAgent);}
			else if(fakeMEDIUMvoteAgent.size() > 0){//偽の霊能者がいれば偽の霊能者に
			    return randomSelect(fakeMEDIUMvoteAgent);}
			else if(MEDIUMvoteAgent.size() > 0){//霊能のグレー
				return randomSelect(MEDIUMvoteAgent);}
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

