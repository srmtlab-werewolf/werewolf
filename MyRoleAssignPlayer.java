package org.aiwolf.myAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.base.player.AbstractRoleAssignPlayer;
import org.aiwolf.client.base.player.AbstractRole;
import org.aiwolf.client.base.player.AbstractSeer;
import org.aiwolf.client.base.player.AbstractVillager;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.net.GameSetting;

import java.util.*;
public class MyRoleAssignPlayer extends AbstractRoleAssignPlayer {

	//public double[][] myThinkJob;

	
	//MySeer meS = new MySeer(this);
	//MyMedium meM = new MyMedium(this);
	//MyVillager meV = new MyVillager(this);
	MyBodyguard meB = new MyBodyguard(this);
	public double BOM = 0.25;//BorderOfMedium
	public double BOS = 0.25;//BorderOfSeer
	public double TWV = 0.3;//TalkWeightVote
	public double TWE = 0.4;//TalkWeightEsti
	public double TWA = 0.1;//TalkWeightAgre
	public double TWDA = 0.1;//TalkWeightDisAgre
	public double TWCN = 0.05;//TalkWeightNumカミングアウト順
	public double TWDV = 0.5;//TalkWeightDiVi
	public double TWI = 0.3;//TalkWeightInq
	public double TWG = 0.3;//TalkWeightGuard
	public double WV = 0.5;//WightVote
	public Role myrole;//自分の役職
	public int MediNum;//現在信頼している霊能者の番号
	public int SeerNum;//現在信頼している占い師の番号
	public int i,k,s;
	public int PP;
	public int BGA = 0;//襲撃者なしが起きると１にする
	public int FirstMEDIUMCODay = 100;//最初に霊能COが出た日
	public int FirstSeerCODay = 100;//最初に占いCOが出た日
	public int MyState = 0;//自分が生きていれば０，死んでたら１
	public List<Integer> A1 = new ArrayList<Integer>();
	public int nowday;//今日の日付
	public double B1[] = new double[6];
	public double C1[] = new double[16];
	public double D1[] = new double[6];
	public double BB;
	public double MyThinkJob[][] = new double[16][6]; //自分から見た他の職業値
	public double ThinkMEDIUMJob[][] = new double[16][6];//霊能以外で使用
	public double ThinkSEERJob[][] = new double[16][6];//占い師以外で使用
	public double ThinkSEMEJob[][] = new double[16][6];//村人と狩人で使用
	public double HaveTrust[][] = new double[16][16];//全員から全員への信頼度，高いと信頼している
	public double prelogit[][] = new double[16][16];//ロジスティック関数で使用，前日までのを記録
	public double logit[][] = new double[16][16];//ロジシュティック関数で使用
	public int DetectWP = 0;//人外露出数，４人見つけたら決め打ちに移行できるので
	public int DetectWPO[] = new int[16];//それぞれからみた人外露出数
	public int MeDetectWP = 0;//霊能決め打ちでの人外露出
	public int SeDetectWP = 0;//占い決めうちでの人外露出
	public int ThinkJ[] = new int[16];
	public int DayTalkNum[][] = new int[15][16];//その日にしたCO、結果報告以外の発言数
	public int MyNum;//自分の番号
	public int readTalkNum = 0;//その日の何番目の会話を読むか
	public int MaxNum = 15;//全員の数
	public int VILLAGERNum = 8;//村人総数
	public int SEERNum = 1;//占い師総数
	public int MEDIUMNum = 1;//霊能者総数
	public int BODYGUARDNum = 1;//狩うど総数
	public int WEREWOLFNum = 3;//狼総数
	public int POSSESSEDNum = 1;//狂人総数
	public int VILLAGERDetectNum = 0;//村人発見数（自分視点）
	public int SEERDetectNum = 0;//占い師発見数
	public int MEDIUMDetectNum = 0;//霊能者発見数
	public int BODYGUARDDetectNum = 0;//狩人発見数
	public int WEREWOLFDetectNum = 0;//人狼発見数
	public int POSSESSEDDetectNum = 0;//狂人発見数
	public int NowNum = 15;//現在生きている数
	public int AliveVILLAGERNum = 8;//生きている村人数
	public int AliveSEERNum = 1;//生きている占い師数
	public int AliveMEDIUMNum = 1;//生きている霊能者数
	public int AliveBODYGUARDNum = 1;//生きている狩人数
	public int AliveWEREWOLFNum = 3;//生きている人狼数
	public int AlivePOSSESSEDNum = 1;//生きている狂人数
	public List<Agent> AllAgent = new ArrayList<Agent>();
	public Map<Agent,List<Utterance>> inq = new HashMap<Agent,List<Utterance>>();//霊能結果
	public Map<Agent,List<Utterance>> div = new HashMap<Agent,List<Utterance>>();//占い結果
	public Map<Agent,List<Utterance>> grd = new HashMap<Agent,List<Utterance>>();//護衛結果
	public Map<Integer,List<Vote>> vot = new HashMap<Integer,List<Vote>>();//毎日の処刑投票の結果を格納する、Integerは日付
	public Map<Agent,List<Utterance>> lasttalkvot = new HashMap<Agent,List<Utterance>>();//その日の最後の投票先発言を記録する
	public int MM = 0;
	public List<Agent> SeerCOAgent = new ArrayList<Agent>();//占いCOした人物
	public List<Agent> AliveSeerCOAgent = new ArrayList<Agent>();//生きている占いCOした人物
	public List<Agent> fakeSeerCOAgent = new ArrayList<Agent>();//偽占いだとわかっている人物
	public List<Agent> AlivefakeSeerCOAgent = new ArrayList<Agent>();//生きている偽占いだと分かった人物
	public List<Agent> fakeMEDIUMAgent = new ArrayList<Agent>();//偽霊能だとわかった人物
	public List<Agent> AlivefakeMEDIUMAgent = new ArrayList<Agent>();//生きている偽霊能だと分かった人物
	public List<Agent> MEDIUMCOAgent = new ArrayList<Agent>();//霊能COした人物
	public List<Agent> AliveMEDIUMCOAgent = new ArrayList<Agent>();//生きている霊能COした人物
	public List<Agent> BODYGUARDCOAgent = new ArrayList<Agent>();//狩人COした人物
	public List<Agent> WhiteAgent = new ArrayList<Agent>();//占い師で使用，自分から白がでた人物
	public List<Agent> BlackAgent = new ArrayList<Agent>();//占い師で使用，自分から黒が出た人物
	public List<Agent> VILLAGERGROUP = new ArrayList<Agent>();//村人陣営
	public List<Agent> WEREWOLFGROUP = new ArrayList<Agent>();//人狼陣営
	public List<Agent> ExecutedAgent = new ArrayList<Agent>();//処刑者リスト
	public List<Agent> AttackedAgent = new ArrayList<Agent>(); //襲撃者リスト
	public Map<Agent,Status> map = new HashMap<Agent, Status>();//エージェントとその状態マップ
	public List<Agent> DIVWEREAgent = new ArrayList<Agent>();//誰でもいいので占い師に黒だしされた人物を格納
	public GameInfo gameInfo1;//その日のgameInfoを格納
	public List<Agent> WEREWOLFAgent = new ArrayList<Agent>();//人狼だと分かっている人物リスト
	public List<Agent> AliveWEREWOLFAgent = new ArrayList<Agent>();//生きている人狼リスト
	public List<Agent> VILLAGERAgent = new ArrayList<Agent>();//村人リスト
	//public List<Agent> MEDIUMAgent = new ArrayList<Agent>();
	public List<Agent> BODYGUARDAgent = new ArrayList<Agent>();//狩人リスト
	public List<Agent> POSSESSEDAgent = new ArrayList<Agent>();//狂人リスト
	public List<Judge> judgeList = new ArrayList<Judge>();//占い師と霊能者でこれに結果を格納するどっちでも使える
	public List<Agent> judgeAgentList = new ArrayList<Agent>();//占い先や霊能先のエージェントを記録
	public List<Agent> COAgent = new ArrayList<Agent>();//複数COやCOせずに結果報告してくるような謎の行動に対処
	public List<Talk> AlldaytalkList = new ArrayList<Talk>();//全部の会話を順番に記録
	public Map<Integer,List<Talk>> daytalkList = new HashMap<Integer,List<Talk>>();//毎日の発言を格納する、Integerは日付，
	public boolean isComingOut = false;//自分がカミングアウトしたかいなか
	public boolean isVote = false;//
	public boolean isVoteSeer = false;
	public Agent Voteagt;
	public List<Judge> myToldJudgeList = new ArrayList<Judge>();
	public double RAN = Math.random() * 5;
	
	
	public MyRoleAssignPlayer(){
		
		//setSeerPlayer(meS);
		//setMediumPlayer(meM);
		//setVillagerPlayer(meV);
		setBodyguardPlayer(meB);
		//setVillagerPlayer(new MyVillager());
		
	}
	
	
	@Override
	public String getName() {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return "NIT TEAM Srmtlab"+ "Player";//this.getClass().getName();
	} 
	
	
	public int Seeraddinq(Agent agt, Utterance ut){//霊能者の発言を記憶占い師が使用
		if(inq.get(agt) == null){
			inq.put(agt, new ArrayList<Utterance>());
		}
		List<Utterance> uttlist = inq.get(agt);
		uttlist.add(ut);
		if(ut.getResult() == Species.WEREWOLF){
			if(fakeSeerCOAgent.contains(ut.getTarget())){
				
			}
			else if(MEDIUMCOAgent.contains(ut.getTarget()) || fakeMEDIUMAgent.contains(ut.getTarget())){
				
			}
			else{
				DetectWPO[agt.getAgentIdx()]++;
			}
			
			
		}
		
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
		}//ここまで
	
	public void addinq(Agent agt, Utterance ut){//霊能者の発言を記憶
		if(inq.get(agt) == null){
			inq.put(agt, new ArrayList<Utterance>());
		}
		List<Utterance> uttlist = inq.get(agt);
		uttlist.add(ut);
		if(ut.getResult() == Species.WEREWOLF){
			if(SeerCOAgent.contains(ut.getTarget()) || fakeSeerCOAgent.contains(ut.getTarget())){
				
			}
			else if(MEDIUMCOAgent.contains(ut.getTarget()) || fakeMEDIUMAgent.contains(ut.getTarget())){
				
			}
			else{
				DetectWPO[agt.getAgentIdx()]++;
			}
			
			
		}
		}//ここまで
	
	public void adddiv(Agent agt, Utterance ut){//占い師の発言を記憶//占い師が使用
		if(div.get(agt) == null){
			div.put(agt, new ArrayList<Utterance>());
		}
		List<Utterance> uttlist = div.get(agt);
		uttlist.add(ut);
		if(ut.getResult() == Species.WEREWOLF){
			if(SeerCOAgent.contains(ut.getTarget()) || fakeSeerCOAgent.contains(ut.getTarget())){
				
			}
			else if(MEDIUMCOAgent.contains(ut.getTarget()) || fakeMEDIUMAgent.contains(ut.getTarget())){
				
			}
			else{
				DetectWPO[agt.getAgentIdx()]++;
			}
			
			
		}
		if(ut.getResult() == Species.WEREWOLF && !DIVWEREAgent.contains(ut.getTarget())){
			DIVWEREAgent.add(ut.getTarget());
		}
	}//ここまで
	
	public void adddivO(Agent agt, Utterance ut){//占い師の発言を記憶
		if(div.get(agt) == null){
			div.put(agt, new ArrayList<Utterance>());
		}
		List<Utterance> uttlist = div.get(agt);
		uttlist.add(ut);
		if(ut.getResult() == Species.WEREWOLF){
			if(SeerCOAgent.contains(ut.getTarget()) || fakeSeerCOAgent.contains(ut.getTarget())){
				
			}
			else if(MEDIUMCOAgent.contains(ut.getTarget()) || fakeMEDIUMAgent.contains(ut.getTarget())){
				
			}
			else if(gameInfo1.getAgent() == ut.getTarget()){//自分を狼だと言ったら
				MyThinkJob[agt.getAgentIdx()][1] = 0;
				CleanJob(agt.getAgentIdx());
				ChangeJob(agt.getAgentIdx(),100,100,100,100);
				if(SeerCOAgent.contains(agt) && !fakeSeerCOAgent.contains(agt)){
					AliveSeerCOAgent.remove(agt);
					SeerCOAgent.remove(agt);
					AlivefakeSeerCOAgent.add(agt);
					fakeSeerCOAgent.add(agt);
					if(SeerCOAgent.size() + fakeSeerCOAgent.size() == 1){
						DetectWP++;
					}
				}
			}
			else{
				DetectWPO[agt.getAgentIdx()]++;
			}
			
			
		}
		else if(ut.getResult() == Species.HUMAN && ut.getTarget() == gameInfo1.getAgent()){
			UJP(agt.getAgentIdx(),Role.SEER,TWDV);
		}
		if(ut.getResult() == Species.WEREWOLF && !DIVWEREAgent.contains(ut.getTarget())){
			DIVWEREAgent.add(ut.getTarget());
		}
	}//ここまで
	public int MEDIadddiv(Agent agt, Utterance ut){//占い師の発言を記憶
		if(div.get(agt) == null){
			div.put(agt, new ArrayList<Utterance>());
		}
		List<Utterance> uttlist = div.get(agt);
		uttlist.add(ut);
		if(ut.getResult() == Species.WEREWOLF){
			if(SeerCOAgent.contains(ut.getTarget()) || fakeSeerCOAgent.contains(ut.getTarget())){
				
			}
			else if(fakeMEDIUMAgent.contains(ut.getTarget())){
				
			}
			else if(gameInfo1.getAgent() == ut.getTarget()){//自分を狼だと言ったら
				MyThinkJob[agt.getAgentIdx()][1] = 0;
				CleanJob(agt.getAgentIdx());
				ChangeJob(agt.getAgentIdx(),100,100,100,100);
				if(SeerCOAgent.contains(agt) && !fakeSeerCOAgent.contains(agt)){
					AliveSeerCOAgent.remove(agt);
					SeerCOAgent.remove(agt);
					AlivefakeSeerCOAgent.add(agt);
					fakeSeerCOAgent.add(agt);
					if(SeerCOAgent.size() + fakeSeerCOAgent.size() == 1){
						DetectWP++;
					}
				}
			}
			else{
				DetectWPO[agt.getAgentIdx()]++;
			}
			
			
		}
		else if(ut.getResult() == Species.HUMAN && ut.getTarget() == gameInfo1.getAgent()){
			UJP(agt.getAgentIdx(),Role.SEER,TWDV);
		}
		if(ut.getResult() == Species.WEREWOLF && !DIVWEREAgent.contains(ut.getTarget())){
			DIVWEREAgent.add(ut.getTarget());
		}
		if(judgeAgentList.size() > 0){
		if(judgeAgentList.contains(ut.getTarget())){
		if(judgeList.get(judgeAgentList.indexOf(ut.getTarget())).getResult() != ut.getResult()){//自分の霊能と占い結果が違ったら
			MyThinkJob[agt.getAgentIdx()][1] = 0;
			CleanJob(agt.getAgentIdx());
			ChangeJob(agt.getAgentIdx(),100,100,100,100);
			return 1;}//占い師の発言が霊能結果と食い違っていたら1を返す
		else if(judgeList.get(judgeAgentList.indexOf(ut.getTarget())).getResult() == ut.getResult()){//自分の霊能と結果が同じであれば
			UJP(agt.getAgentIdx(),Role.SEER,TWI);
			ChangeJob(agt.getAgentIdx(),100,100,100,100);
			return 0;}//占い師の結果が霊能結果と同じなら霊能者の占い師の値を上げる
		}}
	else{
		return 0;}
		return 0;
	}//ここまで
	
	
	
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
				if(D1[k] * (MyThinkJob[i][k] / B1[k]) >= 0.0){
				MyThinkJob[i][k] = D1[k] * (MyThinkJob[i][k] / B1[k]);}
				else{
					MyThinkJob[i][k] = 0.0;
				}
			}
			}}
	for(i = 0;i < 16; i++){
		if(i != R && i != R1 && i != R2 && i != R3 && i != R4){
			CleanJob(i);
		}
	}
	
	}//ここまで
	
	public void CleanJob(int R){//引数の番号のエージェントの調整
		BB = 0;
		for(k = 0; k <= 5; k++){
			if(MyThinkJob[R][k] != 0 && MyThinkJob[R][k] != 1.0){
				BB = BB + MyThinkJob[R][k];}}
		for(k = 0; k <= 5; k++){
			if(MyThinkJob[R][k] != 0 && MyThinkJob[R][k] != 1.0){
				if(1 * (MyThinkJob[R][k] / BB) >= 0.0){
				MyThinkJob[R][k] = 1 * (MyThinkJob[R][k] / BB);}
				else{
					MyThinkJob[R][k] = 0.0;
				}
			
			}}
	}//ここまで
	
	
	
	
	public void usefinish(){//Rは各役職，Nu1は役職の人数，村なら８
		double Min;
		int R;
		Agent agtt = Agent.getAgent(1);
		R = 4;
		for(int gi = 1; gi <= 3; gi++){
			Min = 0.0;
		for(int li = 0;li < AllAgent.size(); li++){
			if(Min < MyThinkJob[AllAgent.get(li).getAgentIdx()][R]){
				agtt = Agent.getAgent(AllAgent.get(li).getAgentIdx());
				Min = MyThinkJob[AllAgent.get(li).getAgentIdx()][R];}
		}
		ThinkJ[agtt.getAgentIdx()] = R;
		AllAgent.remove(agtt);
		}
		
		R = 5;
		for(int gi = 1; gi <= 1; gi++){
			Min = 0.0;
		for(int li = 0;li < AllAgent.size(); li++){
			if(Min < MyThinkJob[AllAgent.get(li).getAgentIdx()][R]){
				agtt = Agent.getAgent(AllAgent.get(li).getAgentIdx());
				Min = MyThinkJob[AllAgent.get(li).getAgentIdx()][R];}
		}
		ThinkJ[agtt.getAgentIdx()] = R;
		AllAgent.remove(agtt);
		}
		
		R = 1;
		for(int gi = 1; gi <= 1; gi++){
			Min = 0.0;
		for(int li = 0;li < AllAgent.size(); li++){
			if(Min < MyThinkJob[AllAgent.get(li).getAgentIdx()][R]){
				agtt = Agent.getAgent(AllAgent.get(li).getAgentIdx());
				Min = MyThinkJob[AllAgent.get(li).getAgentIdx()][R];}
		}
		ThinkJ[agtt.getAgentIdx()] = R;
		AllAgent.remove(agtt);
		}
		
		R = 2;
		for(int gi = 1; gi <= 1; gi++){
			Min = 0.0;
		for(int li = 0;li < AllAgent.size(); li++){
			if(Min < MyThinkJob[AllAgent.get(li).getAgentIdx()][R]){
				agtt = Agent.getAgent(AllAgent.get(li).getAgentIdx());
				Min = MyThinkJob[AllAgent.get(li).getAgentIdx()][R];}
		}
		ThinkJ[agtt.getAgentIdx()] = R;
		AllAgent.remove(agtt);
		}
		R = 3;
		for(int gi = 1; gi <= 1; gi++){
			Min = 0.0;
		for(int li = 0;li < AllAgent.size(); li++){
			if(Min < MyThinkJob[AllAgent.get(li).getAgentIdx()][R]){
				agtt = Agent.getAgent(AllAgent.get(li).getAgentIdx());
				Min = MyThinkJob[AllAgent.get(li).getAgentIdx()][R];}
		}
		ThinkJ[agtt.getAgentIdx()] = R;
		AllAgent.remove(agtt);
		}
		R = 0;
		for(int gi = 1; gi <= 8; gi++){
			Min = 0.0;
		for(int li = 0;li < AllAgent.size(); li++){
			if(Min < MyThinkJob[AllAgent.get(li).getAgentIdx()][R]){
				agtt = Agent.getAgent(AllAgent.get(li).getAgentIdx());
				Min = MyThinkJob[AllAgent.get(li).getAgentIdx()][R];}
		}
		ThinkJ[agtt.getAgentIdx()] = R;
		AllAgent.remove(agtt);
		}
		for(k = 1; k <= MaxNum; k++){
			
			switch(ThinkJ[k]){
			case 0:
				System.out.printf("VILLA|");
				break;
			case 1:
				System.out.printf("SEER |");
				break;
			case 2:
				System.out.printf("MEDIU|");
				break;
			case 3:
				System.out.printf("BODYG|");
				break;
			case 4:
				System.out.printf("WEREW|");
				break;
			case 5:
				System.out.printf("POSSE|");
				break;
			default:
				break;
			}}
		
		
	}//ここまで
	
	//System.out.printf("%d\n",ThinkJ[agtt.getAgentIdx()]);
	
	public void commondayStart(){
		readTalkNum = 0;
		
		nowday = gameInfo1.getDay();
		if(gameInfo1.getAliveAgentList().contains(gameInfo1.getAgent())){
			MyState = 0;
		}else{
			MyState = 1;
			}
		//初期化の前に異常がなかったかだけ確認
		if(daytalkList.get(nowday) == null){
			daytalkList.put(nowday, new ArrayList<Talk>());
		}//毎日の会話内容を入れるリストの作成
	
	
	}//ここまで
	
	public void PCJ(int R, Role job,double L){
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
	
	public void UJP(int R,Role job,double per){//0.0 < per < 1.0　perが大きいほど１に近づく０だと増えない
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
	public void DJP(int R,Role job,double per){//0.0 < per < 1.0　perが大きいほど０に近づく０だと減らない
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
	
	public void CHT(int R, int R1,double per){//0.0 < per < 1.0 perが１にちかいほど信頼度は大きくなる
		if(HaveTrust[R][R1] != 0.0 && HaveTrust[R][R1] != 1.0){
		logit[R][R1] = logit[R][R1] + per;
			HaveTrust[R][R1] = 1 / (1 + Math.exp(-logit[R][R1]));}}//ここまで
	
	public void OtSeerUPD(Talk talk, Utterance utterance, int Rn){
		int INQTF;//霊能結果が合致してたかどうかの確認
		int TAN;
		Agent agentMe = Agent.getAgent(Rn);
		if(!talk.getAgent().equals(gameInfo1.getAgent())){
			switch (utterance.getTopic()){
			case COMINGOUT:
				TAN = utterance.getTarget().getAgentIdx(); //TalkAgentNum
				if(utterance.getRole() == Role.SEER){
					HaveTrust[Rn][talk.getAgent().getAgentIdx()] = 0;
				}//他の占い師への信頼値を０に
				
				if(utterance.getRole() == Role.POSSESSED || utterance.getRole() == Role.WEREWOLF){
					CHT(Rn,talk.getAgent().getAgentIdx(),-TWCN);
				}
				break;
			case DIVINED:
				break;
			case INQUESTED://霊能結果報告
				if(div.get(agentMe).contains(utterance.getTarget())){
					if(div.get(agentMe).get(div.get(agentMe).indexOf(utterance.getTarget())).getResult() != utterance.getResult()){//自分の霊能と占い結果が違ったら
						HaveTrust[Rn][talk.getAgent().getAgentIdx()] = 0;
						}//霊能結果が自分とちがえば信頼度０
					else if(div.get(agentMe).get(div.get(agentMe).indexOf(utterance.getTarget())).getResult() == utterance.getResult()){//自分の霊能と結果が同じであれば
						CHT(Rn,talk.getAgent().getAgentIdx(),TWDV);
						}//霊能の結果が占い結果と同じなら信頼度を上げる
				}
				break;
				
			case ESTIMATE:
				if(utterance.getTarget() != agentMe){
					if(utterance.getRole() == Role.WEREWOLF || utterance.getRole() == Role.POSSESSED){
						if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
						CHT(Rn,utterance.getTarget().getAgentIdx(),logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWE);
						}
						else{
						CHT(Rn,talk.getAgent().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWE);
						}
						}}//自分以外に対して人狼陣営っぽいと言ったら発言者の信頼度が高ければ発言先の信頼度を減少，発言者の信頼度が低ければ発言者の信頼度を減少
					else if(utterance.getTarget() == agentMe){
						CHT(Rn,talk.getAgent().getAgentIdx(),-TWE);
					}
				break;
			case GUARDED:
				
				break;
			case VOTE:
				if(utterance.getTarget() == gameInfo1.getAgent()){
					CHT(Rn,talk.getAgent().getAgentIdx(),-TWV * 1.5);
				}
				else if(div.get(agentMe).contains(utterance.getTarget())){
					CHT(Rn,talk.getAgent().getAgentIdx(),-TWV);
					CHT(talk.getAgent().getAgentIdx(),Rn,-TWV);
				}
				else if(utterance.getTarget() != gameInfo1.getAgent()){
					if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
						CHT(Rn,utterance.getTarget().getAgentIdx(),logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWV);
					}
					else{
						CHT(Rn,talk.getAgent().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWV);
						}
				}
				
				
				break;
			case AGREE:
				
				break;
			case DISAGREE:
				
				break;
			case SKIP:
				
				break;
				default:
					break;
			}}	
	}//ここまで
	
	public void OtMEDIUMUPD(Talk talk, Utterance utterance, int Rn){
		Agent agentMe = Agent.getAgent(Rn);
		if(!talk.getAgent().equals(gameInfo1.getAgent())){
			switch (utterance.getTopic()){
			case COMINGOUT:
				if(utterance.getRole() == Role.MEDIUM){
					HaveTrust[Rn][talk.getAgent().getAgentIdx()] = 0;
					}
				if(utterance.getRole() == Role.POSSESSED || utterance.getRole() == Role.WEREWOLF){
					CHT(Rn,talk.getAgent().getAgentIdx(),-TWCN);
				}
				break;
			case DIVINED:
				if(inq.get(agentMe).size() > 0){
				if(inq.get(agentMe).contains(utterance.getTarget())){
					if(inq.get(agentMe).get(inq.get(agentMe).indexOf(utterance.getTarget())).getResult() != utterance.getResult()){//自分の霊能と占い結果が違ったら
						HaveTrust[Rn][talk.getAgent().getAgentIdx()] = 0;
						}//占い師の発言が霊能結果と食い違っていたら1を返す
					else if(inq.get(agentMe).get(inq.get(agentMe).indexOf(utterance.getTarget())).getResult() == utterance.getResult()){//自分の霊能と結果が同じであれば
						CHT(Rn,talk.getAgent().getAgentIdx(),TWDV);
						}//占い師の結果が霊能結果と同じなら霊能者の占い師の値を上げる
				}}
				break;
			case INQUESTED://霊能結果報告
				
				break;
				
			case ESTIMATE:
				if(utterance.getTarget() != agentMe){
					if(utterance.getRole() == Role.WEREWOLF || utterance.getRole() == Role.POSSESSED){
						if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
						CHT(Rn,utterance.getTarget().getAgentIdx(),logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWE);
						}
						else{
						CHT(Rn,talk.getAgent().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWE);
						}
						}}//自分以外に対して人狼陣営っぽいと言ったら発言者の信頼度が高ければ発言先の信頼度を減少，発言者の信頼度が低ければ発言者の信頼度を減少
					else if(utterance.getTarget() == agentMe){
						CHT(Rn,talk.getAgent().getAgentIdx(),-TWE);
					}
				
				break;
			case GUARDED:
				
				break;
			case VOTE:
				if(utterance.getTarget() == gameInfo1.getAgent()){
					CHT(Rn,talk.getAgent().getAgentIdx(),-TWV * 1.5);
				}
				else if(utterance.getTarget() != gameInfo1.getAgent()){
					if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
						CHT(Rn,utterance.getTarget().getAgentIdx(),logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWV);
					}
					else{
						CHT(Rn,talk.getAgent().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWV);
						}
				}
				break;
			case AGREE:
				
				break;
			case DISAGREE:
				
				break;
			case SKIP:
				
				break;
				default:
					break;
			}}
	}//ここまで
	
	public void OtVILLAGERUPD(Talk talk, Utterance utterance,int Rn){
		if(!talk.getAgent().equals(Agent.getAgent(Rn))){//発言しているのがこのプレイヤーでなければ
			Agent agentMe = Agent.getAgent(Rn);
			switch (utterance.getTopic()){
			case COMINGOUT:
				break;
			case DIVINED:
				if(utterance.getTarget() == agentMe && utterance.getResult() == Species.HUMAN){
					CHT(Rn,talk.getAgent().getAgentIdx(),TWDV);
				}
				else if(utterance.getTarget() == agentMe && utterance.getResult() == Species.WEREWOLF){
					HaveTrust[Rn][talk.getAgent().getAgentIdx()] = 0.0;}
				else if(utterance.getResult() == Species.WEREWOLF){
					CHT(Rn,talk.getAgent().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWDV);
				}//発言者の信頼度が高ければさらに信頼度上昇，低ければ減少
				else if(utterance.getResult() == Species.HUMAN){
					if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
					CHT(Rn,utterance.getTarget().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWDV);
					}}//発言者の信頼度が高ければ占った先の信頼度を上昇
				
				break;
			case INQUESTED://霊能結果報告
				if(utterance.getTarget() == agentMe && utterance.getResult() == Species.HUMAN){
					CHT(Rn,talk.getAgent().getAgentIdx(),TWI);
				}
				else if(utterance.getTarget() == agentMe && utterance.getResult() == Species.WEREWOLF){
					HaveTrust[Rn][talk.getAgent().getAgentIdx()] = 0.0;}
				else if(utterance.getResult() == Species.WEREWOLF){
					if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
					CHT(Rn,utterance.getTarget().getAgentIdx(),logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWDV);
				}}//発言者の信頼度が高ければ霊能先の信頼度を減少
				
				break;
				
			case ESTIMATE:
				if(utterance.getTarget() != agentMe){
				if(utterance.getRole() == Role.WEREWOLF || utterance.getRole() == Role.POSSESSED){
					if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
					CHT(Rn,utterance.getTarget().getAgentIdx(),logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWE);
					}
					else{
					CHT(Rn,talk.getAgent().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWE);
					}
					}}//自分以外に対して人狼陣営っぽいと言ったら発言者の信頼度が高ければ発言先の信頼度を減少，発言者の信頼度が低ければ発言者の信頼度を減少
				else if(utterance.getTarget() == agentMe){
					CHT(Rn,talk.getAgent().getAgentIdx(),-TWE);
				}
				break;
			case GUARDED:
				break;
			case VOTE:
				if(utterance.getTarget() == gameInfo1.getAgent()){
					CHT(Rn,talk.getAgent().getAgentIdx(),-TWV);
				}
				else if(utterance.getTarget() != gameInfo1.getAgent()){
					if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
						CHT(Rn,utterance.getTarget().getAgentIdx(),logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWV);
					}
					else{
						CHT(Rn,talk.getAgent().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWV);
						}
				}
				break;
			case AGREE:
				
				break;
			case DISAGREE:
				
				break;
			case SKIP:
				break;
				default:
					break;
			}
		}
	}//ここまで
	
	public void OtBODYGUARDUPD(Talk talk, Utterance utterance,int Rn){
		if(!talk.getAgent().equals(Agent.getAgent(Rn))){//発言しているのがこのプレイヤーでなければ
			Agent agentMe = Agent.getAgent(Rn);
			switch (utterance.getTopic()){
			case COMINGOUT:
				if(utterance.getRole() == Role.BODYGUARD){
					HaveTrust[Rn][talk.getAgent().getAgentIdx()] = 0;
				}
				break;
			case DIVINED:
				if(utterance.getTarget() == agentMe && utterance.getResult() == Species.HUMAN){
					CHT(Rn,talk.getAgent().getAgentIdx(),TWDV);
				}
				else if(utterance.getTarget() == agentMe && utterance.getResult() == Species.WEREWOLF){
					HaveTrust[Rn][talk.getAgent().getAgentIdx()] = 0.0;}
				else if(utterance.getResult() == Species.WEREWOLF){
					CHT(Rn,talk.getAgent().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWDV);
				}//発言者の信頼度が高ければさらに信頼度上昇，低ければ減少
				else if(utterance.getResult() == Species.HUMAN){
					if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
					CHT(Rn,utterance.getTarget().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWDV);
					}}//発言者の信頼度が高ければ占った先の信頼度を上昇
				
				break;
			case INQUESTED://霊能結果報告
				if(utterance.getTarget() == agentMe && utterance.getResult() == Species.HUMAN){
					CHT(Rn,talk.getAgent().getAgentIdx(),TWI);
				}
				else if(utterance.getTarget() == agentMe && utterance.getResult() == Species.WEREWOLF){
					HaveTrust[Rn][talk.getAgent().getAgentIdx()] = 0.0;}
				else if(utterance.getResult() == Species.WEREWOLF){
					if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
					CHT(Rn,utterance.getTarget().getAgentIdx(),logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWDV);
				}}//発言者の信頼度が高ければ霊能先の信頼度を減少
				
				break;
				
			case ESTIMATE:
				if(utterance.getTarget() != agentMe){
				if(utterance.getRole() == Role.WEREWOLF || utterance.getRole() == Role.POSSESSED){
					if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
					CHT(Rn,utterance.getTarget().getAgentIdx(),logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWE);
					}
					else{
					CHT(Rn,talk.getAgent().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWE);
					}
					}}//自分以外に対して人狼陣営っぽいと言ったら発言者の信頼度が高ければ発言先の信頼度を減少，発言者の信頼度が低ければ発言者の信頼度を減少
				else if(utterance.getTarget() == agentMe){
					CHT(Rn,talk.getAgent().getAgentIdx(),-TWE);
				}
				break;
			case GUARDED:
				if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] != 0){
					HaveTrust[Rn][talk.getAgent().getAgentIdx()] = 0;
				}
				break;
			case VOTE:
				if(utterance.getTarget() == gameInfo1.getAgent()){
					CHT(Rn,talk.getAgent().getAgentIdx(),-TWV);
				}
				else if(utterance.getTarget() != gameInfo1.getAgent()){
					if(HaveTrust[Rn][talk.getAgent().getAgentIdx()] > HaveTrust[Rn][utterance.getTarget().getAgentIdx()]){
						CHT(Rn,utterance.getTarget().getAgentIdx(),logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWV);
					}
					else{
						CHT(Rn,talk.getAgent().getAgentIdx(),-logitspace(Rn,utterance.getTarget().getAgentIdx(),talk.getAgent().getAgentIdx())*TWV);
						}
				}
				break;
			case AGREE:
				
				break;
			case DISAGREE:
				
				break;
			case SKIP:
				break;
				default:
					break;
			}
		}
	}//ここまで
	
	public double logitspace(int L1,int L2,int L3){//L1からL2への信頼度とL1からL3への信頼度の差を取る
		double LS = logit[L1][L3] - logit[L1][L2];
	return LS;
	}//ここまで
	
	public void first(){
		
		
		
		
	}
	
	
}
