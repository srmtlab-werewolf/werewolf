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
	
	public MySeer(MyRoleAssignPlayer mrap) {
		super();
		this.mrap = mrap;
		
	}
	//List<List<Utterance>> inq = new ArrayList<List<Utterance>>();
	
	Map<Agent,List<Utterance>> inq = new HashMap<Agent,List<Utterance>>();//霊能結果
	Map<Agent,List<Utterance>> div = new HashMap<Agent,List<Utterance>>();//占い結果
	Map<Agent,List<Utterance>> grd = new HashMap<Agent,List<Utterance>>();//護衛結果
	Map<Integer,List<Vote>> vot = new HashMap<Integer,List<Vote>>();//毎日の処刑投票の結果を格納する、Integerは日付
	Map<Agent,List<Utterance>> lasttalkvot = new HashMap<Agent,List<Utterance>>();//その日の最後の投票先発言を記録する．
	// if (inq.get(a) == null) {
	//   inq.put(a, new ArrayList<Utterance));  // initialize
	// }
	// List<Utterance> uttlist = inq.get(a);
	// uttlist.add(hatsugen);
	
	// for (Utterance u : uttlist) {
	//   
	// }
	
	
	public int addinq(Agent agt, Utterance ut){//霊能者の発言を記憶
		if(inq.get(agt) == null){
			inq.put(agt, new ArrayList<Utterance>());
		}
		List<Utterance> uttlist = inq.get(agt);
		uttlist.add(ut);
		if(judgeAgentList.contains(ut.getTarget())){
			if(judgeList.get(judgeAgentList.indexOf(ut.getTarget())).getResult() != ut.getResult()){//自分の占いと霊能結果が違ったら
				MyThinkJob[agt.getAgentIdx()][2] = 0;
				CleanJob(agt.getAgentIdx());
				ChangeJob(agt.getAgentIdx(),100,100,100,100);
				return 1;}//霊能者の発言が占い結果と食い違っていたら1を返す
			else if(judgeList.get(judgeAgentList.indexOf(ut.getTarget())).getResult() == ut.getResult()){//自分の占いと結果が同じであれば
				UJP(agt.getAgentIdx(),Role.MEDIUM,TWI);
				ChangeJob(agt.getAgentIdx(),100,100,100,100);
				return 0;}//霊能者の結果が占い結果と同じなら霊能者の霊能者値を上げる
			}
		else{
			return 0;}
		return 0;
		}
	
	public void adddiv(Agent agt, Utterance ut){//偽占い師の発言を記憶
		if(div.get(agt) == null){
			div.put(agt, new ArrayList<Utterance>());
		}
		List<Utterance> uttlist = div.get(agt);
		uttlist.add(ut);
	}
	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting){
		super.initialize(gameInfo, gameSetting);
		MyNum = gameInfo.getAgent().getAgentIdx();
		MaxNum = NowNum = gameSetting.getPlayerNum();
		VILLAGERNum =  AliveVILLAGERNum = gameSetting.getRoleNum(Role.VILLAGER);
		SEERNum = AliveSEERNum = gameSetting.getRoleNum(Role.SEER);
		MEDIUMNum = AliveMEDIUMNum = gameSetting.getRoleNum(Role.MEDIUM);
		BODYGUARDNum = AliveBODYGUARDNum = gameSetting.getRoleNum(Role.BODYGUARD);
		WEREWOLFNum = AliveWEREWOLFNum = gameSetting.getRoleNum(Role.WEREWOLF);
		POSSESSEDNum = AlivePOSSESSEDNum = gameSetting.getRoleNum(Role.POSSESSED);
		for(i = 1; i <= MaxNum;i++){
		if(lasttalkvot.get(Agent.getAgent(i)) == null){
			lasttalkvot.put(Agent.getAgent(i), new ArrayList<Utterance>());
		}}
		for( i = 1;i <= MaxNum;i++){
			if(i != MyNum){
			MyThinkJob[i][0] = 8.0 / (MaxNum - 1.0);
			MyThinkJob[i][1] = 0;
			MyThinkJob[i][2] = 1.0 / (MaxNum - 1.0);
			MyThinkJob[i][3] = 1.0 / (MaxNum - 1.0);
			MyThinkJob[i][4] = 3.0 / (MaxNum - 1.0);
			MyThinkJob[i][5] = 1.0 / (MaxNum - 1.0);
			
			}
			else{
				MyThinkJob[i][0] = 0;
				MyThinkJob[i][1] = 1.0;
				MyThinkJob[i][2] = 0;
				MyThinkJob[i][3] = 0;
				MyThinkJob[i][4] = 0;
				MyThinkJob[i][5] = 0;
			}
			}
		for(i=0;i < 16;i++){
			for(k= 0;k < 6;k++){
				ThinkMEDIUMJob[i][k] = MyThinkJob[i][k]; 
			}}
		for(i=0;i < 16;i++){
			for(k= 0;k < 16;k++){
				HaveTrust[i][k] = 0.5;//信頼の基準値を0.5とする
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
		for(i = 0;i < MEDIUMCOAgent.size(); i++){
			if(divineGrayCandidates.contains(MEDIUMCOAgent.get(i))){
				divineGrayCandidates.remove(MEDIUMCOAgent.get(i));
				divineCandidates.remove(MEDIUMCOAgent.get(i));
			}
		}
		for(i = 0;i < fakeSeerCOAgent.size();i++){
			if(divineGrayCandidates.contains(fakeSeerCOAgent.get(i))){
				divineGrayCandidates.remove(fakeSeerCOAgent.get(i));
				divineGrayCandidates.remove(fakeSeerCOAgent.get(i));}
			for(k = 0;k < div.get(fakeSeerCOAgent.get(i)).size();k++){
				if(divineGrayCandidates.contains(div.get(fakeSeerCOAgent.get(i)).get(k).getTarget())){
					divineGrayCandidates.remove(div.get(fakeSeerCOAgent.get(i)).get(k).getTarget());
				}
			}
		}
		
		double ran = ((gameInfo1.getDay() - 1) * 0.4) + 0.2;
		if(ran > 1.0){
			ran = 1.0;}
		if(divineGrayCandidates.size() > 0){//完グレ選択
			int talkNum = 0;
			Agent Grayagt = divineGrayCandidates.get(0);
			for(i = 0; i < divineGrayCandidates.size();i++){//俗にいう多弁占い
				if(talkNum < DayTalkNum[nowday][divineGrayCandidates.get(i).getAgentIdx()]){
					talkNum = DayTalkNum[nowday][divineGrayCandidates.get(i).getAgentIdx()];
				Grayagt = divineGrayCandidates.get(i);
				}}
			if(talkNum > 0 && ran > Math.random()){
				return Grayagt;
			}
			return randomSelect(divineGrayCandidates);}
		else if(divineCandidates.size() > 0){//自分からのグレー選択
			int talkNum = 0;
			Agent divagt = divineCandidates.get(0);
			for(i = 0; i < divineCandidates.size();i++){//俗にいう多弁占い
				if(talkNum < DayTalkNum[nowday][divineCandidates.get(i).getAgentIdx()]){
					talkNum = DayTalkNum[nowday][divineCandidates.get(i).getAgentIdx()];
				divagt = divineCandidates.get(i);
				}}
			if(talkNum > 0 && ran > Math.random()){
				return divagt;
			}
			return randomSelect(divineCandidates);
		}else{
			return getMe();//なければ自分
		}
	}
	
	public void ChangeJob(int R, int R1, int R2, int R3, int R4){
		for(i = 0; i < 6; i++){
			B1[i] = 0;
		}
		for(i = 1; i <= MaxNum; i++){
			for(k = 0; k <= 5; k++){
				if(MyThinkJob[i][k] != 0 && MyThinkJob[i][k] != 1.0 && i != R && i != R1 && i != R2 && i != R3 && i != R4){
					B1[k] = B1[k] + MyThinkJob[i][k];
				}}}
			D1[0] = 8 - (1.0 * VILLAGERDetectNum) - (MyThinkJob[R][0] % 1.0);
			D1[1] = 1 - (1.0 * SEERDetectNum) - (MyThinkJob[R][1] % 1.0);
			D1[2] = 1 - (1.0 * MEDIUMDetectNum) - (MyThinkJob[R][2] % 1.0);
			D1[3] = 1 - (1.0 * BODYGUARDDetectNum) - (MyThinkJob[R][3] % 1.0);
			D1[4] = 3 - (1.0 * WEREWOLFDetectNum) - (MyThinkJob[R][4] % 1.0);
			D1[5] = 1 - (1.0 * POSSESSEDDetectNum) - (MyThinkJob[R][5] % 1.0);
			if(R1 <= 16){
				for(i = 0;i < 6;i++){
				D1[i] = D1[i] - (MyThinkJob[R1][i] % 1.0);}}
			if(R2 <= 16){
				for(i = 0;i < 6;i++){
					D1[i] = D1[i] - (MyThinkJob[R2][i] % 1.0);}}
			if(R3 <= 16){
				for(i = 0;i < 6;i++){
					D1[i] = D1[i] - (MyThinkJob[R3][i] % 1.0);}}
			if(R4 <= 16){
				for(i = 0;i < 6;i++){
					D1[i] = D1[i] - (MyThinkJob[R4][i] % 1.0);}}
			for(i = 1; i <= 15; i++){
				for(k = 0; k<= 5; k++){
			if(MyThinkJob[i][k] != 0 && MyThinkJob[i][k] != 1.0 && i != R && i != R1 && i != R2 && i != R3 && i != R4){
				MyThinkJob[i][k] = D1[k] * (MyThinkJob[i][k] / B1[k]);}
			}}
	for(i = 0;i < 16; i++){
		if(i != R && i != R1 && i != R2 && i != R3 && i != R4){
			CleanJob(i);
		}
	}
	
	}
	
	public void CleanJob(int R){//引数の番号のエージェントの調整
		BB = 0;
		for(k = 0; k <= 5; k++){
			if(MyThinkJob[R][k] != 0 && MyThinkJob[R][k] != 1.0){
				BB = BB + MyThinkJob[R][k];}}
		for(k = 0; k <= 5; k++){
			if(MyThinkJob[R][k] != 0 && MyThinkJob[R][k] != 1.0){
				MyThinkJob[R][k] = 1 * (MyThinkJob[R][k] / BB);}}
	}
	
	public void output(){
		double sum;
		double sum1[] = new double[16];;
		if(MediNum != 0){
			System.out.printf("ThinkMEDIUMJob\n");
			for(i = 1; i <= MaxNum; i++){
				System.out.printf("AGT%2d|",i);}
			System.out.printf("\n");
			for(i = 0; i < 6; i++){
				sum = 0;
				for(k = 1; k < 16; k++){
			System.out.printf("%.3f|",ThinkMEDIUMJob[k][i]);
			sum = sum + ThinkMEDIUMJob[k][i];
			sum1[k] = sum1[k] + ThinkMEDIUMJob[k][i]; 
				}
				System.out.printf("合計%.3f", sum);
				System.out.println("\n");}
			for(k = 1; k <= MaxNum; k++){
				System.out.printf("%.3f|",sum1[k]);}
			System.out.printf("\n");	
		}
		else if(MediNum == 0){
		System.out.printf("MyThinkJob\n");
		for(i = 1; i <= MaxNum; i++){
			System.out.printf("AGT%2d|",i);}
		System.out.printf("\n");
		for(i = 0; i < 6; i++){
			sum = 0;
			for(k = 1; k < 16; k++){
		System.out.printf("%.3f|",MyThinkJob[k][i]);
		sum = sum + MyThinkJob[k][i];
		sum1[k] = sum1[k] + MyThinkJob[k][i]; 
			}
			System.out.printf("合計%.3f", sum);
			System.out.println("\n");}
		for(k = 1; k <= MaxNum; k++){
			System.out.printf("%.3f|",sum1[k]);}
		System.out.printf("\n");}
		for(i = 0; i < judgeList.size(); i++){
		System.out.printf("%s:%s",judgeAgentList.get(i),judgeList.get(i).getResult());
		System.out.printf("\n");}
		System.out.printf("偽占いCOリスト");
		System.out.println(fakeSeerCOAgent);
		System.out.printf("生きている偽占いCOリスト");
		System.out.println(AlivefakeSeerCOAgent);
		System.out.printf("霊能COリスト");
		System.out.println(MEDIUMCOAgent);
		System.out.printf("生きている霊能COリスト");
		System.out.println(AliveMEDIUMCOAgent);
		System.out.printf("偽霊能COリスト");
		System.out.println(fakeMEDIUMAgent);
		System.out.printf("生きている偽霊能COリスト");
		System.out.println(AlivefakeMEDIUMAgent);
		System.out.printf("見つけた狼リスト");
		System.out.println(WEREWOLFAgent);
		System.out.printf("生きている見つけた狼リスト");
		System.out.println(AliveWEREWOLFAgent);
		System.out.printf("見つけた人外の数:%d\n",DetectWP);
		System.out.printf("信頼値表（仮）\n");
		
		for(i = 1; i < 16; i++){
			for(k = 1; k < 16; k++){
				System.out.printf("%.3f|", HaveTrust[k][i]);}
			System.out.printf("\n");
		}
	}
	
	@Override
	public void finish() {
		double sum;
		double sum1[] = new double[16];;
		for(i = 1; i <= MaxNum; i++){
			System.out.printf("AGT%2d|",i);}
		System.out.printf("\n");
		for(i = 0; i < 6; i++){
			sum = 0;
			for(k = 1; k < 16; k++){
		System.out.printf("%.3f|",MyThinkJob[k][i]);
		sum = sum + MyThinkJob[k][i];
		sum1[k] = sum1[k] + MyThinkJob[k][i]; 
			}
			System.out.printf("合計%.3f", sum);
			System.out.println("\n");}
		for(k = 1; k <= MaxNum; k++){
			System.out.printf("%.3f|",sum1[k]);}
		System.out.printf("\n");
		for(i = 0; i < judgeList.size(); i++){
		System.out.printf("%s:%s",judgeAgentList.get(i),judgeList.get(i).getResult());
		System.out.printf("\n");}
		System.out.printf("偽占いCOリスト");
		System.out.println(fakeSeerCOAgent);
		System.out.printf("霊能COリスト");
		System.out.println(MEDIUMCOAgent);
		System.out.printf("見つけた狼リスト");
		System.out.println(WEREWOLFAgent);
	}
	
    public void MeUp(Agent agt){//引数の霊能者の情報をThinkMEDIUMJobに反映させる
    	int MeI;
    	int Mk;
    	for(MeI = 0; MeI < inq.get(agt).size();MeI++){
    		if(judgeAgentList.contains(inq.get(agt).get(MeI).getTarget())){
    	if(inq.get(agt).get(MeI).getResult() == Species.HUMAN){
    	ThinkMEDIUMJob[inq.get(agt).get(MeI).getTarget().getAgentIdx()][4] = 0.0;}
    	else if(inq.get(agt).get(MeI).getResult() == Species.WEREWOLF){
    		
    		for(Mk = 0; Mk < 6; Mk++){
    		ThinkMEDIUMJob[inq.get(agt).get(MeI).getTarget().getAgentIdx()][Mk] = 0.0;
    		}
    		ThinkMEDIUMJob[inq.get(agt).get(MeI).getTarget().getAgentIdx()][4] = 1.0;
    	}
    	}	
    	}
    	
    	
    }
	
	
	int DayTalkNum[][] = new int[15][16];//その日にしたCO、結果報告以外の発言数
	int MyNum;
	int readTalkNum = 0;
	int MaxNum = 15;
	int VILLAGERNum = 8;
	int SEERNum = 1;
	int MEDIUMNum = 1;
	int BODYGUARDNum = 1;
	int WEREWOLFNum = 3;
	int POSSESSEDNum = 1;
	int VILLAGERDetectNum = 0;
	int SEERDetectNum = 1;
	int MEDIUMDetectNum = 0;
	int BODYGUARDDetectNum = 0;
	int WEREWOLFDetectNum = 0;
	int POSSESSEDDetectNum = 0;
	int NowNum = 15;
	int AliveVILLAGERNum = 8;
	int AliveSEERNum = 1;
	int AliveMEDIUMNum = 1;
	int AliveBODYGUARDNum = 1;
	int AliveWEREWOLFNum = 3;
	int AlivePOSSESSEDNum = 1;
	double BOM = 0.25;
	int DetectWP = 0;//４人見つけたら決め打ちに移行できるので
	int MeDetectWP = 0;//霊能決め打ちでの人外露出
	List<Integer> A1 = new ArrayList<Integer>();
	int nowday;
	double B1[] = new double[6];
	double C1[] = new double[16];
	double D1[] = new double[6];
	double BB;
	double MyThinkJob[][] = new double[16][6]; 
	double ThinkMEDIUMJob[][] = new double[16][6];
    double HaveTrust[][] = new double[16][16];
	double TWV = 0.3;//TalkWeightVote
	double TWE = 0.4;//TalkWeightEsti
	double TWA = 0.1;//TalkWeightAgre
	double TWDA = 0.1;//TalkWeightDisAgre
	double TWCN = 0.05;//TalkWeightNumカミングアウト順
	double TWDV = 0.5;//TalkWeightDiVi
	double TWI = 0.3;//TalkWeightInq
	double TWG = 0.3;//TalkWeightGuard
	double WV = 0.5;//WightVote
	int MediNum;//現在信頼している霊能者の番号
	int i,k,s;
	int PP;
	int BGA = 0;//襲撃者なしが起きると１にする
	int FirstMEDIUMCODay = 100;
	int FirstSeerCODay = 100;
	void think(int nowday) {
		if(nowday >= 2){//二日目以降は死者がでるので
			System.out.printf("Think");
			System.out.println(gameInfo1.getExecutedAgent());
			System.out.println(vot.get(nowday - 1));
			ExecutedAgent.add(gameInfo1.getExecutedAgent());
			if(AliveWEREWOLFAgent.contains(gameInfo1.getExecutedAgent())){//処刑されたのが狼ならば
				AliveWEREWOLFAgent.remove(gameInfo1.getExecutedAgent());//生きている狼のリストから消す
			}
			if(AlivefakeSeerCOAgent.contains(gameInfo1.getExecutedAgent())){//偽占い師
				AlivefakeSeerCOAgent.remove(gameInfo1.getExecutedAgent());
			}
			if(AliveMEDIUMCOAgent.contains(gameInfo1.getExecutedAgent())){//霊能CO
				AliveMEDIUMCOAgent.remove(gameInfo1.getExecutedAgent());
				if(AlivefakeMEDIUMAgent.contains(gameInfo1.getExecutedAgent())){//また、偽霊能なら
					AlivefakeMEDIUMAgent.remove(gameInfo1.getExecutedAgent());
				}
			}
			
			NowNum--;
			if(vot.get(nowday - 1) == null){//今日の日付の前の日の投票結果はまだ入ってないだろうので
				vot.put(nowday - 1, new ArrayList<Vote>());//新しい日付の完成
			}
			vot.get(nowday - 1).addAll(gameInfo1.getVoteList());
			for(i = 0; i < vot.get(nowday - 1).size(); i++){
				
			DHT(vot.get(nowday - 1).get(i).getAgent().getAgentIdx(),vot.get(nowday - 1).get(i).getTarget().getAgentIdx(),WV);
			if(vot.get(nowday - 1).get(i).getTarget() == getMe() && isComingOut == true){//カミングアウトした自分に投票する相手の人外値を上昇
				UJP(vot.get(nowday - 1).get(i).getTarget().getAgentIdx(),Role.WEREWOLF,WV);
				UJP(vot.get(nowday - 1).get(i).getTarget().getAgentIdx(),Role.POSSESSED,WV);}
			else if(judgeAgentList.contains(vot.get(nowday - 1).get(i).getTarget()) && isComingOut == true){//投票が自分の占い先でかつ、自分が白を出していた時に
				if(judgeList.get(judgeAgentList.indexOf(vot.get(nowday - 1).get(i).getTarget())).getResult() == Species.HUMAN){
					DHT(vot.get(nowday - 1).get(i).getAgent().getAgentIdx(),getMe().getAgentIdx(),WV);
					UJP(vot.get(nowday - 1).get(i).getAgent().getAgentIdx(),Role.WEREWOLF,0.2);
					UJP(vot.get(nowday - 1).get(i).getAgent().getAgentIdx(),Role.POSSESSED,0.2);
				}
				else if(judgeList.get(judgeAgentList.indexOf(vot.get(nowday - 1).get(i).getTarget())).getResult() == Species.WEREWOLF){//投票が自分の占い先でかつ、自分が黒をだしている
					UHT(vot.get(nowday - 1).get(i).getAgent().getAgentIdx(),getMe().getAgentIdx(),WV);
					DJP(vot.get(nowday - 1).get(i).getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
					DJP(vot.get(nowday - 1).get(i).getAgent().getAgentIdx(),Role.POSSESSED,0.1);
				}}
			for(s = 0; s < fakeSeerCOAgent.size();s++){
				
				for(k = 0; k < div.get(fakeSeerCOAgent.get(s)).size();k++){
				if(div.get(fakeSeerCOAgent.get(s)).get(k).getTarget() == vot.get(nowday - 1).get(i).getTarget() && div.get(fakeSeerCOAgent.get(s)).get(k).getResult() == Species.HUMAN){
					DHT(vot.get(nowday - 1).get(i).getAgent().getAgentIdx(),fakeSeerCOAgent.get(s).getAgentIdx(),WV);
				}
				else if(div.get(fakeSeerCOAgent.get(s)).get(k).getTarget() == vot.get(nowday - 1).get(i).getTarget() && div.get(fakeSeerCOAgent.get(s)).get(k).getResult() == Species.WEREWOLF){
					UHT(vot.get(nowday - 1).get(i).getAgent().getAgentIdx(),fakeSeerCOAgent.get(s).getAgentIdx(),WV);
				}
				}}//偽占い師の白に投票すると言った場合
			}
			System.out.printf("TTT");
			if(gameInfo1.getAttackedAgent() != null){
				if(AlivefakeSeerCOAgent.contains(gameInfo1.getAttackedAgent())){//偽占い師
					AlivefakeSeerCOAgent.remove(gameInfo1.getAttackedAgent());
				}
				if(AliveMEDIUMCOAgent.contains(gameInfo1.getAttackedAgent())){//霊能CO
					AliveMEDIUMCOAgent.remove(gameInfo1.getAttackedAgent());
					if(AlivefakeMEDIUMAgent.contains(gameInfo1.getAttackedAgent())){//また、偽霊能なら
						AlivefakeMEDIUMAgent.remove(gameInfo1.getAttackedAgent());
					}
				}
			AttackedAgent.add(gameInfo1.getAttackedAgent());
			MyThinkJob[gameInfo1.getAttackedAgent().getAgentIdx()][4] = 0;
			CleanJob(gameInfo1.getAttackedAgent().getAgentIdx());
			ChangeJob(gameInfo1.getAttackedAgent().getAgentIdx(),100,100,100,100);
			NowNum--;
				if(BGA > 0){
				BGA -= 1; 
				}
			}
			else{
				BGA = 2;
			}
			
			for(i = 0; i < vot.get(nowday -1).size(); i++){
				if(lasttalkvot.get(vot.get(nowday - 1).get(i).getAgent()).size() > 0){
			if(lasttalkvot.get(vot.get(nowday - 1).get(i).getAgent()).get(0).getTarget() != vot.get(nowday -1).get(i).getTarget()){//投票先が発言と違ったら
				UJP(vot.get(nowday - 1).get(i).getAgent().getAgentIdx(),Role.WEREWOLF,0.2);
			}}}
			for(i = 1;i <= MaxNum;i++){//毎日初期化
				lasttalkvot.get(Agent.getAgent(i)).clear();
			}
			
		}
	}
	
	@Override
	public void dayStart(){
		super.dayStart();
		readTalkNum = 0;
		isVote = false;
		int MyState = 0;
		MM = 1;
		nowday = gameInfo1.getDay();
		if(gameInfo1.getAliveAgentList().contains(getMe())){
			MyState = 0;
		}else{
			MyState = 1;
			}
		//初期化の前に異常がなかったかだけ確認
		
		
		if(MyState == 0){
			//mrap.commonThink(nowday);
			//think(nowday);
			if(nowday >= 1){
				judgeList.add(gameInfo1.getDivineResult());
				judgeAgentList.add(gameInfo1.getDivineResult().getTarget());
				System.out.printf("日にち%d\n", nowday);
			if(gameInfo1.getDivineResult().getResult() == Species.HUMAN){
				System.out.printf("人間発見\n");
				WhiteAgent.add(gameInfo1.getDivineResult().getTarget());
						MyThinkJob[gameInfo1.getDivineResult().getTarget().getAgentIdx()][4] = 0.0;
						System.out.printf("番号%d\n", gameInfo1.getDivineResult().getTarget().getAgentIdx());
						//for(i = 0; i < 6; i++){
							//System.out.printf("%.3f", MyThinkJob[gameInfo1.getDivineResult().getAgent().getAgentIdx()][4]);}
						CleanJob(gameInfo1.getDivineResult().getTarget().getAgentIdx());}
			else if(gameInfo1.getDivineResult().getResult() == Species.WEREWOLF){
				BlackAgent.add(gameInfo1.getDivineResult().getTarget());
				WEREWOLFAgent.add(gameInfo1.getDivineResult().getTarget());
				AliveWEREWOLFAgent.add(gameInfo1.getDivineResult().getTarget());
				DetectWP++;
				if(MEDIUMCOAgent.contains(gameInfo1.getDivineResult().getTarget()) && !fakeMEDIUMAgent.contains(gameInfo1.getDivineResult().getTarget())){
					MEDIUMCOAgent.remove(gameInfo1.getDivineResult().getTarget());
					AliveMEDIUMCOAgent.remove(gameInfo1.getDivineResult().getTarget());
					fakeMEDIUMAgent.add(gameInfo1.getDivineResult().getTarget());
					AlivefakeMEDIUMAgent.add(gameInfo1.getDivineResult().getTarget());
				}
				for(i = 0;i <= 5; i++){
					if(i != 4){
						MyThinkJob[gameInfo1.getDivineResult().getTarget().getAgentIdx()][i] = 0;}}
				//changeProb(gameInfo1.getDivineResult().getTarget(), Species.WEREWOLF, 1.0);
				MyThinkJob[gameInfo1.getDivineResult().getTarget().getAgentIdx()][4] = 1.0;
				WEREWOLFDetectNum++;}
			ChangeJob(gameInfo1.getDivineResult().getTarget().getAgentIdx(),100,100,100,100);
			
		}
			think(nowday);
		}
		output();
	}
	int MM = 0;
	List<Agent> fakeSeerCOAgent = new ArrayList<Agent>();
	List<Agent> AlivefakeSeerCOAgent = new ArrayList<Agent>();
	List<Agent> fakeMEDIUMAgent = new ArrayList<Agent>();
	List<Agent> AlivefakeMEDIUMAgent = new ArrayList<Agent>();
	List<Agent> MEDIUMCOAgent = new ArrayList<Agent>();
	List<Agent> AliveMEDIUMCOAgent = new ArrayList<Agent>();
	List<Agent> BODYGUARDCOAgent = new ArrayList<Agent>();
	List<Agent> WhiteAgent = new ArrayList<Agent>();
	List<Agent> BlackAgent = new ArrayList<Agent>();
	List<Agent> VILLAGERGROUP = new ArrayList<Agent>();
	List<Agent> WEREWOLFGROUP = new ArrayList<Agent>();
	List<Agent> ExecutedAgent = new ArrayList<Agent>();
	List<Agent> AttackedAgent = new ArrayList<Agent>(); 
	Map<Agent,Status> map = new HashMap<Agent, Status>();
	GameInfo gameInfo1;
	List<Agent> WEREWOLFAgent = new ArrayList<Agent>();
	List<Agent> AliveWEREWOLFAgent = new ArrayList<Agent>();
	List<Agent> VILLAGERAgent = new ArrayList<Agent>();
	List<Agent> MEDIUMAgent = new ArrayList<Agent>();
	List<Agent> BODYGUARDAgent = new ArrayList<Agent>();
	List<Agent> POSSESSEDAgent = new ArrayList<Agent>();
	List<Judge> judgeList = new ArrayList<Judge>();
	List<Agent> judgeAgentList = new ArrayList<Agent>();
	List<Agent> COAgent = new ArrayList<Agent>();//複数COやCOせずに結果報告してくるような謎の行動に対処
	void PCJ(int R, Role job,double L){
		if(job == Role.VILLAGER){
			MyThinkJob[R][0] = L;}
		else if(job == Role.SEER){
			MyThinkJob[R][1] = L;}
		else if(job == Role.MEDIUM){
			MyThinkJob[R][2] = L;}
		else if(job == Role.BODYGUARD){
			MyThinkJob[R][3] = L;}
		else if(job == Role.WEREWOLF){
			MyThinkJob[R][4] = L;}
		else if(job == Role.POSSESSED){
			MyThinkJob[R][5] = L;}
	}//PointChangeJob
	
	void UJP(int R,Role job,double per){//0.0 < per < 1.0　perが大きいほど１に近づく０だと増えない
		double JP = 0.0;
		int ii;
		switch(job){
		case VILLAGER:
			if(MyThinkJob[R][0] != 0){
			for(ii = 0; ii < 6; ii++){
			if(ii != 0 && MyThinkJob[R][ii] != 1.0 && MyThinkJob[R][ii] != 0){
				JP = JP + (MyThinkJob[R][ii] * per);
				MyThinkJob[R][ii] = MyThinkJob[R][ii] - (MyThinkJob[R][ii] * per);
			}}
			MyThinkJob[R][0] = MyThinkJob[R][0] + JP;}
			break;
		case SEER:
				if(MyThinkJob[R][1] != 0){
				for(ii = 0; ii < 6; ii++){
				if(ii != 1 && MyThinkJob[R][ii] != 1.0 && MyThinkJob[R][ii] != 0){
					JP = JP + (MyThinkJob[R][ii] * per);
					MyThinkJob[R][ii] = MyThinkJob[R][ii] - (MyThinkJob[R][ii] * per);
				}}
				MyThinkJob[R][1] = MyThinkJob[R][1] + JP;}
				break;
		case MEDIUM:
			if(MyThinkJob[R][2] != 0){
			for(ii = 0; ii < 6; ii++){
			if(ii != 2 && MyThinkJob[R][ii] != 1.0 && MyThinkJob[R][ii] != 0){
				JP = JP + (MyThinkJob[R][ii] * per);
				MyThinkJob[R][ii] = MyThinkJob[R][ii] - (MyThinkJob[R][ii] * per);
			}}
			MyThinkJob[R][2] = MyThinkJob[R][2] + JP;}
			break;	
		case BODYGUARD:
			if(MyThinkJob[R][3] != 0){
			for(ii = 0; ii < 6; ii++){
			if(ii != 3 && MyThinkJob[R][ii] != 1.0 && MyThinkJob[R][ii] != 0){
				JP = JP + (MyThinkJob[R][ii] * per);
				MyThinkJob[R][ii] = MyThinkJob[R][ii] - (MyThinkJob[R][ii] * per);
			}}
			MyThinkJob[R][3] = MyThinkJob[R][3] + JP;}
			break;	
		case WEREWOLF:
			if(MyThinkJob[R][4] != 0){
			for(ii = 0; ii < 6; ii++){
			if(ii != 4 && MyThinkJob[R][ii] != 1.0 && MyThinkJob[R][ii] != 0){
				JP = JP + (MyThinkJob[R][ii] * per);
				MyThinkJob[R][ii] = MyThinkJob[R][ii] - (MyThinkJob[R][ii] * per);
			}}
			MyThinkJob[R][4] = MyThinkJob[R][4] + JP;}
			break;
		case POSSESSED:
			if(MyThinkJob[R][5] != 0){
			for(ii = 0; ii < 6; ii++){
			if(ii != 5 && MyThinkJob[R][ii] != 1.0 && MyThinkJob[R][ii] != 0){
				JP = JP + (MyThinkJob[R][ii] * per);
				MyThinkJob[R][ii] = MyThinkJob[R][ii] - (MyThinkJob[R][ii] * per);
			}}
			MyThinkJob[R][5] = MyThinkJob[R][5] + JP;}
			break;
		}
		
		
		
	}//UpJobPoint
	
	void DJP(int R,Role job,double per){//0.0 < per < 1.0　perが大きいほど０に近づく０だと減らない
		double JP = 0.0;
		double KK = 0.0; 
		int im = 0;
		switch(job){
		case VILLAGER:
			if(MyThinkJob[R][0] != 1.0){
				KK = MyThinkJob[R][0] * per;
				MyThinkJob[R][0] = MyThinkJob[R][0] - KK;
			for(im = 0; im < 6; im++){
			if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
				JP = JP + MyThinkJob[R][im];
			}}
			for(im = 0; im < 6; im++){
				if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
					MyThinkJob[R][im] = MyThinkJob[R][im] + (KK * MyThinkJob[R][im]) / JP;
				}}}
			break;
		case SEER:
			if(MyThinkJob[R][1] != 1.0){
				KK = MyThinkJob[R][1] * per;
				MyThinkJob[R][1] = MyThinkJob[R][1] - KK;
			for(im = 0; im < 6; im++){
			if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
				JP = JP + MyThinkJob[R][im];
			}}
			for(im = 0; im < 6; im++){
				if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
					MyThinkJob[R][im] = MyThinkJob[R][im] + (KK * MyThinkJob[R][im]) / JP;
				}}}
				break;
		case MEDIUM:
			if(MyThinkJob[R][2] != 1.0){
				KK = MyThinkJob[R][2] * per;
				MyThinkJob[R][2] = MyThinkJob[R][2] - KK;
			for(im = 0; im < 6; im++){
			if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
				JP = JP + MyThinkJob[R][im];
			}}
			for(im = 0; im < 6; im++){
				if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
					MyThinkJob[R][im] = MyThinkJob[R][im] + (KK * MyThinkJob[R][im]) / JP;
				}}}
			break;	
		case BODYGUARD:
			if(MyThinkJob[R][3] != 1.0){
				KK = MyThinkJob[R][3] * per;
				MyThinkJob[R][3] = MyThinkJob[R][3] - KK;
			for(im = 0; im < 6; im++){
			if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
				JP = JP + MyThinkJob[R][im];
			}}
			for(im = 0; im < 6; im++){
				if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
					MyThinkJob[R][im] = MyThinkJob[R][im] + (KK * MyThinkJob[R][im]) / JP;
				}}}
			break;	
		case WEREWOLF:
			if(MyThinkJob[R][4] != 1.0){
				KK = MyThinkJob[R][4] * per;
				MyThinkJob[R][4] = MyThinkJob[R][4] - KK;
			for(im = 0; im < 6; im++){
			if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
				JP = JP + MyThinkJob[R][im];
			}}
			for(im = 0; im < 6; im++){
				if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
					MyThinkJob[R][im] = MyThinkJob[R][im] + (KK * MyThinkJob[R][im]) / JP;
				}}}
			break;
		case POSSESSED:
			if(MyThinkJob[R][5] != 1.0){
				KK = MyThinkJob[R][5] * per;
				MyThinkJob[R][5] = MyThinkJob[R][5] - KK;
			for(im = 0; im < 6; im++){
			if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
				JP = JP + MyThinkJob[R][im];
			}}
			for(im = 0; im < 6; im++){
				if(im != 0 && MyThinkJob[R][im] != 1.0 && MyThinkJob[R][im] != 0){
					MyThinkJob[R][im] = MyThinkJob[R][im] + (KK * MyThinkJob[R][im]) / JP;
				}}}
			break;
		}
		
		
		
	}//UpJobPoint
	
	void UHT(int R, int R1,double per){//0.0 < per < 1.0 perが１にちかいほど信頼度は大きくなる
		if(HaveTrust[R][R1] != 0.0 && HaveTrust[R][R1] != 1.0){
		HaveTrust[R][R1] += (1 - HaveTrust[R][R1]) * per;}}
	void DHT(int R, int R1,double per){//0.0 < per < 1.0 perが１に近いほど信頼度は小さくなる
		if(HaveTrust[R][R1] != 0.0 && HaveTrust[R][R1] != 1.0){
		HaveTrust[R][R1] = HaveTrust[R][R1] * (1 - per);}}
	
	
	@Override
	public void update(GameInfo gameInfo) {//
		super.update(gameInfo);
		gameInfo1 = gameInfo;
		MediNum = 0;
		MeDetectWP = DetectWP;//発見した数を自分で見つけた数に初期化
		List<Talk> talkList = gameInfo.getTalkList();
		//List<Judge> judge1 = new ArrayList<Judge>();
		//Judge judge2 = gameInfo1.getDivineResult();
		//PP = 1;
		int INQTF;//霊能結果が合致してたかどうかの確認
		int TAN;//TalkAgentNum
		for(i=0;i < 16;i++){
			for(k= 0;k < 6;k++){
				ThinkMEDIUMJob[i][k] = MyThinkJob[i][k]; 
			}}
		//System.out.println("�肢����" + judge1.get(nowday).getDay()+"���" + judge1.get(nowday).getAgent() + judge1.get(nowday).getResult());
		for(int i = readTalkNum; i< talkList.size(); i++){
			Talk talk = talkList.get(i);
			Utterance utterance = new Utterance(talk.getContent());
			if(!talk.getAgent().equals(getMe())){
			switch (utterance.getTopic()){
			case COMINGOUT:
				TAN = utterance.getTarget().getAgentIdx(); 
				if(utterance.getRole() == Role.SEER && !fakeSeerCOAgent.contains(talk.getAgent())){
					if(FirstSeerCODay == 100){
						FirstSeerCODay = nowday;}
					fakeSeerCOAgent.add(utterance.getTarget());
					AlivefakeSeerCOAgent.add(utterance.getTarget());
					if(COAgent.contains(utterance.getTarget())){//すでに他でCOしている人物がCOしてきたら，人外度を上昇させる
						UJP(TAN,Role.POSSESSED,0.4);
						UJP(TAN,Role.WEREWOLF,0.4);
					}
					else{//他でCOしてなくてまだリストがなければ作成
					COAgent.add(utterance.getTarget());}
					if(div.get(utterance.getTarget()) == null){
						div.put(utterance.getTarget(), new ArrayList<Utterance>());
					}
					if(fakeSeerCOAgent.size() == 1){//占いCO一人目なら
						DetectWP++;
						HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
						PCJ(TAN,Role.VILLAGER,0.0);
						PCJ(TAN,Role.SEER,0.0);
						PCJ(TAN,Role.MEDIUM,0.0);
						PCJ(TAN,Role.BODYGUARD,0.0);
					CleanJob(TAN);
					ChangeJob(TAN,100,100,100,100);}
					else if(fakeSeerCOAgent.size() == 2){
						DetectWP++;
						HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
						for(int M1 = 0; M1 < fakeSeerCOAgent.size();M1++){
							for(int M2 = 0; M2 < fakeSeerCOAgent.size();M2++){
								if(M1 != M2){
									DHT(fakeSeerCOAgent.get(M1).getAgentIdx(),fakeSeerCOAgent.get(M2).getAgentIdx(),1.0);
								}}}
						PCJ(TAN,Role.VILLAGER,0.0);
						PCJ(TAN,Role.SEER,0.0);
						PCJ(TAN,Role.MEDIUM,0.0);
						PCJ(TAN,Role.BODYGUARD,0.0);
					CleanJob(TAN);
						ChangeJob(TAN,fakeSeerCOAgent.get(0).getAgentIdx(),100,100,100);}
					else if(fakeSeerCOAgent.size() == 3){
						DetectWP++;
						HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
						for(int M1 = 0; M1 < fakeSeerCOAgent.size();M1++){
							for(int M2 = 0; M2 < fakeSeerCOAgent.size();M2++){
								if(M1 != M2){
									DHT(fakeSeerCOAgent.get(M1).getAgentIdx(),fakeSeerCOAgent.get(M2).getAgentIdx(),1.0);
								}}}
						PCJ(TAN,Role.VILLAGER,0.0);
						PCJ(TAN,Role.SEER,0.0);
						PCJ(TAN,Role.MEDIUM,0.0);
						PCJ(TAN,Role.BODYGUARD,0.0);
					CleanJob(TAN);
						ChangeJob(TAN,fakeSeerCOAgent.get(0).getAgentIdx(),fakeSeerCOAgent.get(1).getAgentIdx(),100,100);}
					else if(fakeSeerCOAgent.size() == 4){
						DetectWP++;
						HaveTrust[talk.getAgent().getAgentIdx()][getMe().getAgentIdx()] = 0.0;
						for(int M1 = 0; M1 < fakeSeerCOAgent.size();M1++){
							for(int M2 = 0; M2 < fakeSeerCOAgent.size();M2++){
								if(M1 != M2){
									DHT(fakeSeerCOAgent.get(M1).getAgentIdx(),fakeSeerCOAgent.get(M2).getAgentIdx(),1.0);
								}}}
						PCJ(TAN,Role.VILLAGER,0.0);
						PCJ(TAN,Role.SEER,0.0);
						PCJ(TAN,Role.MEDIUM,0.0);
						PCJ(TAN,Role.BODYGUARD,0.0);
					CleanJob(TAN);
						ChangeJob(TAN,fakeSeerCOAgent.get(0).getAgentIdx(),fakeSeerCOAgent.get(1).getAgentIdx(),fakeSeerCOAgent.get(2).getAgentIdx(),100);}
					if(WEREWOLFAgent.contains(talk.getAgent())){
						DetectWP--;//先に占って狼だとわかっていたら値をマイナス１して調整
					}
				}//偽占い師が出たらその職業値を人狼と狂人以外０にする
				if(utterance.getRole() == Role.MEDIUM && !MEDIUMCOAgent.contains(talk.getAgent()) && !fakeMEDIUMAgent.contains(talk.getAgent())){
					if(FirstMEDIUMCODay == 100){
						FirstMEDIUMCODay = nowday;}
					if(COAgent.contains(utterance.getTarget())){//すでに他でCOしている人物がCOしてきたら，人外度を上昇させる
						UJP(TAN,Role.POSSESSED,0.4);
						UJP(TAN,Role.WEREWOLF,0.4);
					}
					else{//他でCOしてなければリストに追加
						COAgent.add(utterance.getTarget());}
					if(inq.get(utterance.getTarget()) == null){
						inq.put(utterance.getTarget(), new ArrayList<Utterance>());
					}
					if(WEREWOLFAgent.contains(talk.getAgent())){//霊能COの前に人狼だと分かっていたら
						fakeMEDIUMAgent.add(talk.getAgent());
						AlivefakeMEDIUMAgent.add(talk.getAgent());}
					else{
					MEDIUMCOAgent.add(utterance.getTarget());
					AliveMEDIUMCOAgent.add(utterance.getTarget());
				if(MEDIUMCOAgent.size() == 1){//霊能CO一人目なら
					PCJ(TAN,Role.VILLAGER,0.0);
					PCJ(TAN,Role.SEER,0.0);
					PCJ(TAN,Role.BODYGUARD,0.0);
					CleanJob(TAN);
					ChangeJob(TAN,100,100,100,100);}
				else if(MEDIUMCOAgent.size() == 2){//霊能CO二人目なら
					DetectWP++;
					for(int M1 = 0; M1 < MEDIUMCOAgent.size();M1++){
						for(int M2 = 0; M2 < MEDIUMCOAgent.size();M2++){
							if(M1 != M2){
								DHT(MEDIUMCOAgent.get(M1).getAgentIdx(),MEDIUMCOAgent.get(M2).getAgentIdx(),1.0);
							}}}
					PCJ(TAN,Role.VILLAGER,0.0);
					PCJ(TAN,Role.SEER,0.0);
					PCJ(TAN,Role.BODYGUARD,0.0);
					CleanJob(TAN);
					ChangeJob(TAN,MEDIUMCOAgent.get(0).getAgentIdx(),100,100,100);}
				else if(MEDIUMCOAgent.size() == 3){//霊能CO三人目なら
					DetectWP++;
					for(int M1 = 0; M1 < MEDIUMCOAgent.size();M1++){
						for(int M2 = 0; M2 < MEDIUMCOAgent.size();M2++){
							if(M1 != M2){
								DHT(MEDIUMCOAgent.get(M1).getAgentIdx(),MEDIUMCOAgent.get(M2).getAgentIdx(),1.0);
							}}}
					PCJ(TAN,Role.VILLAGER,0.0);
					PCJ(TAN,Role.SEER,0.0);
					PCJ(TAN,Role.BODYGUARD,0.0);
					CleanJob(TAN);
					ChangeJob(TAN,MEDIUMCOAgent.get(0).getAgentIdx(),MEDIUMCOAgent.get(1).getAgentIdx(),100,100);}
				else if(MEDIUMCOAgent.size() == 4){//霊能CO４人目なら
					DetectWP++;
					for(int M1 = 0; M1 < MEDIUMCOAgent.size();M1++){
						for(int M2 = 0; M2 < MEDIUMCOAgent.size();M2++){
							if(M1 != M2){
								DHT(MEDIUMCOAgent.get(M1).getAgentIdx(),MEDIUMCOAgent.get(M2).getAgentIdx(),1.0);
							}}}
					PCJ(TAN,Role.VILLAGER,0.0);
					PCJ(TAN,Role.SEER,0.0);
					PCJ(TAN,Role.BODYGUARD,0.0);
					CleanJob(TAN);
					ChangeJob(TAN,MEDIUMCOAgent.get(0).getAgentIdx(),MEDIUMCOAgent.get(1).getAgentIdx(),100,100);}
					}
				}
				if(utterance.getRole() == Role.BODYGUARD && !BODYGUARDCOAgent.contains(talk.getAgent())){
					if(COAgent.contains(utterance.getTarget())){//すでに他でCOしている人物がCOしてきたら，人外度を上昇させる
						UJP(TAN,Role.POSSESSED,0.4);
						UJP(TAN,Role.WEREWOLF,0.4);
					}
					else{//他でCOしてなければリストに追加
						COAgent.add(utterance.getTarget());}
					BODYGUARDCOAgent.add(utterance.getTarget());
					if(BODYGUARDCOAgent.size() == 1){
						PCJ(TAN,Role.VILLAGER,0.0);
						PCJ(TAN,Role.SEER,0.0);
						PCJ(TAN,Role.MEDIUM,0.0);
						CleanJob(TAN);
					ChangeJob(TAN,100,100,100,100);}
					if(BODYGUARDCOAgent.size() == 2){
						for(int M1 = 0; M1 < BODYGUARDCOAgent.size();M1++){
							for(int M2 = 0; M2 < BODYGUARDCOAgent.size();M2++){
								if(M1 != M2){
									DHT(BODYGUARDCOAgent.get(M1).getAgentIdx(),BODYGUARDCOAgent.get(M2).getAgentIdx(),1.0);
								}}}
						PCJ(TAN,Role.VILLAGER,0.0);
						PCJ(TAN,Role.SEER,0.0);
						PCJ(TAN,Role.BODYGUARD,0.0);
						CleanJob(TAN);
						ChangeJob(utterance.getTarget().getAgentIdx(),BODYGUARDCOAgent.get(0).getAgentIdx(),100,100,100);}
				}
				if(utterance.getRole() == Role.POSSESSED){
					UJP(TAN,Role.POSSESSED,0.2);
					UJP(TAN,Role.WEREWOLF,0.5);
				}
				if(utterance.getRole() == Role.WEREWOLF){
					UJP(TAN,Role.POSSESSED,0.7);
				}
				break;
			case DIVINED:
				if(!fakeSeerCOAgent.contains(talk.getAgent())){//まだCOしてないのに占い結果報告なんてしてきたら
					UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.1);
					UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
				}//人外の値上昇
				adddiv(talk.getAgent(), utterance);
				
				break;
			case INQUESTED://霊能結果報告
				if(!fakeMEDIUMAgent.contains(talk.getAgent()) && !MEDIUMCOAgent.contains(talk.getAgent())){//まだCOしてないのに霊能結果報告なんてしてきたら
					UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.1);
					UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
				}//人外の値上昇
				TAN = utterance.getTarget().getAgentIdx(); 
				INQTF = addinq(talk.getAgent(),utterance);
				if(INQTF == 1 && !fakeMEDIUMAgent.contains(talk.getAgent()) && MEDIUMCOAgent.contains(talk.getAgent())){//まだ偽だと分かっていない人の霊能結果が食い違っていた場合
					BlackAgent.add(talk.getAgent());
					MEDIUMCOAgent.remove(talk.getAgent());
					AliveMEDIUMCOAgent.remove(talk.getAgent());
					fakeMEDIUMAgent.add(talk.getAgent());
					AlivefakeMEDIUMAgent.add(talk.getAgent());}
				/*if(MEDIUMCOAgent.size() == 1 && MyThinkJob[TAN][2] > 0.65){
					if(utterance.getResult() == Species.HUMAN && !judgeAgentList.contains(utterance.getTarget())){
					WEREWOLFDetectNum++;
					BlackAgent.add(utterance.getTarget());
					WEREWOLFAgent.add(utterance.getTarget());
						MyThinkJob[utterance.getTarget().getAgentIdx()][4] = 0;
					CleanJob(utterance.getTarget().getAgentIdx());
					ChangeJob(utterance.getTarget().getAgentIdx(),100,100,100,100);}
					else if(utterance.getResult() == Species.WEREWOLF && !judgeAgentList.contains(utterance.getTarget())){
						if(!WEREWOLFAgent.contains(utterance.getTarget())){
						for(i = 0;i <= 5; i++){
							if(i != 4){
								MyThinkJob[utterance.getTarget().getAgentIdx()][i] = 0;}}
						MyThinkJob[utterance.getTarget().getAgentIdx()][4] = 1.0;
						CleanJob(utterance.getTarget().getAgentIdx());
						ChangeJob(utterance.getTarget().getAgentIdx(),100,100,100,100);
						WEREWOLFAgent.add(utterance.getTarget());
						}
					}
				}*/
				break;
				
			case ESTIMATE:
				DayTalkNum[nowday][talk.getAgent().getAgentIdx()]++;
				if(utterance.getRole() == Role.WEREWOLF || utterance.getRole() == Role.POSSESSED){
					DHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),TWE);}
				else{
					UHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),TWE);
				}
				break;
			case GUARDED:
				if(!BODYGUARDCOAgent.contains(talk.getAgent())){//まだCOしてないのに霊能結果報告なんてしてきたら
					UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.1);
					UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
				}//人外の値上昇
				if(BGA > 0){
					UJP(talk.getAgent().getAgentIdx(),Role.BODYGUARD,(BGA / 2.0) * TWG);
				}
				break;
			case VOTE:
				DayTalkNum[nowday][talk.getAgent().getAgentIdx()]++;
				DHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),TWV);
				if(lasttalkvot.get(talk.getAgent()).size() >= 1){//既にこの日投票発言をしていたら
					lasttalkvot.get(talk.getAgent()).clear();
					lasttalkvot.get(talk.getAgent()).add(utterance);
				}
				else{
					lasttalkvot.get(talk.getAgent()).add(utterance);
				}
				if(utterance.getTarget() == getMe() && isComingOut == true){//カミングアウトした自分に投票する相手の人外値を上昇
					UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,TWV);
					UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,TWV);}
				else if(judgeAgentList.contains(utterance.getTarget()) && isComingOut == true){//投票が自分の占い先でかつ、自分が白を出していた時に
					if(judgeList.get(judgeAgentList.indexOf(utterance.getTarget())).getResult() == Species.HUMAN){
						DHT(talk.getAgent().getAgentIdx(),getMe().getAgentIdx(),TWV);
						UJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.2);
						UJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.2);
					}
					if(judgeList.get(judgeAgentList.indexOf(utterance.getTarget())).getResult() == Species.WEREWOLF){//投票が自分の占い先でかつ、自分が黒をだしている
						UHT(talk.getAgent().getAgentIdx(),getMe().getAgentIdx(),TWV);
						DJP(talk.getAgent().getAgentIdx(),Role.WEREWOLF,0.1);
						DJP(talk.getAgent().getAgentIdx(),Role.POSSESSED,0.1);
					}}
				for(s = 0; s < fakeSeerCOAgent.size();s++){
					
					for(k = 0; k < div.get(fakeSeerCOAgent.get(s)).size();k++){
					if(div.get(fakeSeerCOAgent.get(s)).get(k).getTarget() == utterance.getTarget() && div.get(fakeSeerCOAgent.get(s)).get(k).getResult() == Species.HUMAN){
						DHT(talk.getAgent().getAgentIdx(),fakeSeerCOAgent.get(s).getAgentIdx(),TWV);
					}
					else if(div.get(fakeSeerCOAgent.get(s)).get(k).getTarget() == utterance.getTarget() && div.get(fakeSeerCOAgent.get(s)).get(k).getResult() == Species.WEREWOLF){
						UHT(talk.getAgent().getAgentIdx(),fakeSeerCOAgent.get(s).getAgentIdx(),TWV);
					}
					}}//偽占い師の白に投票すると言った場合
				
				break;
			case AGREE:
				DayTalkNum[nowday][talk.getAgent().getAgentIdx()]++;
				UHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),TWA);
				break;
			case DISAGREE:
				DayTalkNum[nowday][talk.getAgent().getAgentIdx()]++;
				DHT(talk.getAgent().getAgentIdx(),utterance.getTarget().getAgentIdx(),TWDA);
				break;
			case SKIP:
				DJP(talk.getAgent().getAgentIdx(), Role.VILLAGER, 0.05);
				break;
				default:
					break;
			}}
			readTalkNum++;
		}//ここまでトークの整理
		if(MEDIUMCOAgent.size() > 0){
		for(i = 0; i < MEDIUMCOAgent.size(); i++){
		if(BOM < MyThinkJob[MEDIUMCOAgent.get(i).getAgentIdx()][2]){//霊能者の信頼度が高ければ
			MeUp(MEDIUMCOAgent.get(i));
			MediNum = MEDIUMCOAgent.get(i).getAgentIdx();
		}
		}}
	} //update 
	
	boolean isComingOut = false;
	boolean isVote = false;
	boolean isVoteMEDI = false;
	boolean isVoteSeer = false;
	Agent Voteagt;
	List<Judge> myToldJudgeList = new ArrayList<Judge>();
	
	@Override
	public String talk() {
		String voteTalk;
		if(!isComingOut){
			for(Judge judge: getMyJudgeList()){
				if(judge.getResult() == Species.WEREWOLF || fakeSeerCOAgent.size() > 0){
					String comingoutTalk = TemplateTalkFactory.comingout(getMe(), getMyRole());
					isComingOut = true;
					return comingoutTalk;
				}}
		}
		else{
			for(Judge judge:getMyJudgeList()){
				if(!myToldJudgeList.contains(judge)){
					String resultTalk = TemplateTalkFactory.divined(judge.getTarget(), judge.getResult());
					myToldJudgeList.add(judge);
					return resultTalk;
				}}}
		if(AliveWEREWOLFAgent.size() > 0 && !isVote){//まだ生きている分かっている狼がいれば
			System.out.printf("とりあえず狼にいれよう\n");
			Voteagt = randomSelect(AliveWEREWOLFAgent);
			voteTalk = TemplateTalkFactory.vote(Voteagt);
			isVote = true;
			return voteTalk;
		}
		else if(DetectWP >= 4 && !isVote){//人外を４以上（普通なら４で止まる）見つけた時、ただ既に見つけた狼は処刑した状態なはず
			System.out.printf("人外全露出じゃないですか？\n");
			if(MEDIUMCOAgent.size() >= 2 && AlivefakeMEDIUMAgent.size() > 0){//偽霊能に投票
				isVote = true;
				Voteagt = randomSelect(AlivefakeMEDIUMAgent);
				voteTalk = TemplateTalkFactory.vote(Voteagt);
				return voteTalk;}
			else if(MEDIUMCOAgent.size() >= 2 && AliveMEDIUMCOAgent.size() > 0){//霊能グレーに投票
				isVote = true;
				Voteagt = randomSelect(AliveMEDIUMCOAgent);
				voteTalk = TemplateTalkFactory.vote(Voteagt);
				return voteTalk;}
			else if(fakeSeerCOAgent.size() >= 2 && AlivefakeSeerCOAgent.size() > 0){//偽占い師に投票
				Voteagt = randomSelect(AlivefakeSeerCOAgent);
				isVote = true;
				voteTalk = TemplateTalkFactory.vote(Voteagt);
				return voteTalk;}
		}
		else if(DetectWP <= 3 && !isVote && MEDIUMCOAgent.size() >= 2 && AliveMEDIUMCOAgent.size() >= 1 && nowday >= 2){//霊ロラ始めまたは完遂
			System.out.printf("霊ロラしようぜ\n");
			if(AlivefakeMEDIUMAgent.size() > 0){
				isVote = true;
				Voteagt = randomSelect(AlivefakeMEDIUMAgent);
				voteTalk = TemplateTalkFactory.vote(Voteagt);
				return voteTalk;}
			else if(AliveMEDIUMCOAgent.size() > 0){
				Voteagt = randomSelect(AliveMEDIUMCOAgent);
				isVote = true;
				voteTalk = TemplateTalkFactory.vote(Voteagt);
				return voteTalk;}
		}
		else if(DetectWP <= 3 && !isVote && fakeSeerCOAgent.size() >= 2 && AlivefakeSeerCOAgent.size() > 1 && nowday >= 2){//占いロラ
			System.out.printf("占いロラしようぜ\n");
			isVote = true;
			Voteagt = randomSelect(AlivefakeSeerCOAgent);
			voteTalk = TemplateTalkFactory.vote(Voteagt);
			return voteTalk;
		}
		
		
		return Talk.OVER;
	}

	@Override
	public Agent vote() {
		//List<Agent> voteCandidates = new ArrayList<Agent>();
		double VoteJob[][] = new double[16][6];
		if(MediNum == 0){//現在信頼できそうな霊能者がいればその結果を反映、
		for(i=0;i < 16;i++){
			for(k= 0;k < 6;k++){
				VoteJob[i][k] = MyThinkJob[i][k]; 
			}}}
		else if(MediNum != 0){
			for(i=0;i < 16;i++){
				for(k= 0;k < 6;k++){
					VoteJob[i][k] = ThinkMEDIUMJob[i][k]; 
				}}}
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
    if(fakeSeerCOAgent.contains(judge.getTarget())){   
	switch (judge.getResult()){
        case HUMAN:
        	whiteSeerAgent.add(judge.getTarget());
        			break;
        case WEREWOLF:
        	blackAgent.add(judge.getTarget());
        	break;}}
    else if(MEDIUMCOAgent.contains(judge.getTarget())){
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
		if(MEDIUMCOAgent.size() > 0){
			for(int i = 0; i < MEDIUMCOAgent.size(); i++){
			if(getLatestDayGameInfo().getAliveAgentList().contains(MEDIUMCOAgent.get(i)) && !blackAgent.contains(MEDIUMCOAgent.get(i)) && !whiteMEDIUMAgent.contains(MEDIUMCOAgent.get(i))){
			if(!fakeMEDIUMAgent.contains(MEDIUMCOAgent.get(i))){
				MEDIUMvoteAgent.add(MEDIUMCOAgent.get(i));}
			else{
				fakeMEDIUMvoteAgent.add(MEDIUMCOAgent.get(i));}}
			}}
		if(fakeSeerCOAgent.size() > 0){
			for(int i = 0; i < fakeSeerCOAgent.size(); i++){
			if(getLatestDayGameInfo().getAliveAgentList().contains(fakeSeerCOAgent.get(i)) && !blackAgent.contains(fakeSeerCOAgent.get(i)) && !whiteSeerAgent.contains(fakeSeerCOAgent.get(i))){
			fakeSeerCOvoteAgent.add(fakeSeerCOAgent.get(i));
			}}}
		List<Agent> voteCandidates = new ArrayList<Agent>();
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
		
		System.out.printf("投票先狼:%s\n", blackAgent);
		System.out.printf("投票先霊能偽:%s\n", fakeMEDIUMvoteAgent);
		System.out.printf("投票先グレー、役職持ち除く:%s\n", voteCandidates);
		System.out.printf("投票先霊能グレー:%s\n", MEDIUMvoteAgent);
		System.out.printf("投票先占いグレー:%s\n", fakeSeerCOvoteAgent);
		System.out.printf("投票先占い白:%s\n", whiteSeerAgent);
		System.out.printf("投票先霊能白:%s\n", whiteMEDIUMAgent);
		System.out.printf("投票先グレー白:%s\n", whiteAgent);
			if(isVote == true){
			return Voteagt;
			}
			else if(DetectWP < 4){
			if(blackAgent.size() > 0){//人狼を見つけていれば人狼に
				return randomSelect(blackAgent);}
			else if(fakeMEDIUMvoteAgent.size() > 0){//偽の霊能者がいれば偽の霊能者に
				return randomSelect(fakeMEDIUMvoteAgent);}
			else if(voteCandidates.size() > 0){
				Agent agt1 = voteCandidates.get(0);
				if(nowday == 1){
					return randomSelect(voteCandidates);}
				else if(nowday == 2){
					int talkN = 10;
					for(i = 0; i < voteCandidates.size();i++){
						if(talkN > DayTalkNum[nowday][voteCandidates.get(i).getAgentIdx()]){
							talkN = DayTalkNum[nowday][voteCandidates.get(i).getAgentIdx()];
							agt1 = voteCandidates.get(i);
						}
						else if(talkN > DayTalkNum[nowday][voteCandidates.get(i).getAgentIdx()] && Math.random() > 0.5){
							agt1 = voteCandidates.get(i);
						}
					}
					return agt1;
				}
				else{
					double sum1 = 0;
					for(i = 0;i < voteCandidates.size(); i++){
						if(sum1 < (VoteJob[voteCandidates.get(i).getAgentIdx()][4] + VoteJob[voteCandidates.get(i).getAgentIdx()][5])){
							sum1 = VoteJob[voteCandidates.get(i).getAgentIdx()][4] + VoteJob[voteCandidates.get(i).getAgentIdx()][5];
							agt1 = voteCandidates.get(i);
						}}//グレーの中で一番怪しいと思っている人物を探す
					if(sum1 > Math.random()){
						return agt1;}
					else{
						return randomSelect(voteCandidates);}}}
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

