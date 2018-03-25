package cn.worldwalker.game.wyqp.mj.robot;

import cn.worldwalker.game.wyqp.common.domain.base.BaseRequest;
import cn.worldwalker.game.wyqp.common.domain.mj.MjMsg;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRequest;
import cn.worldwalker.game.wyqp.common.enums.GameTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.MsgTypeEnum;
import cn.worldwalker.game.wyqp.common.utils.HttpClientUtils;
import cn.worldwalker.game.wyqp.mj.enums.MjTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.java_websocket.WebSocket;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Client {
    private Socket socket;
    private String token;
    private Integer roomId;
    private Integer playerId;
    private List<Integer> cardList;
    private List<Integer> cueCardList;
    private List<String> pengList;

    private Boolean gameOver;
    private Integer position;
    private Random random;

//    private static final String HTTP_URL = "http://game.nf.worldwalker.cn";
//    private static final String WS_URL = "ws://39.107.96.117:10008";
    private static final String HTTP_URL = "http://localhost";
    private static final String WS_URL = "ws://127.0.0.1:10008";

    public Client(Integer position) {
        this.position = position;
        random = new Random();
        pengList = new ArrayList<>(16);
        gameOver = false;
    }

    public List<String> getPengList() {
        return pengList;
    }

    public Integer getPosition() {
        return position;
    }

    public void init() throws Exception {
        generateToken();
        socket = new Socket(new URI(WS_URL),this);
        socket.connect();
        int i=0;
        while (!socket.getReadyState().equals(WebSocket.READYSTATE.OPEN)  && i++ < 10) {
            Thread.sleep(100);
        }
        System.out.println(token.substring(0,4) + ": 连接成功");
        gameOver = false;
    }

    private MjRequest createRequest(MsgTypeEnum msgTypeEnum){
        MjRequest mjRequest = new MjRequest();
        mjRequest.setGameType(GameTypeEnum.mj.gameType);
        mjRequest.setToken(token);
        mjRequest.setMsgType(msgTypeEnum.msgType);
        mjRequest.setDetailType(MjTypeEnum.shangHaiBaiDa.type);

        MjMsg mjMsg = new MjMsg();
        mjMsg.setPlayerId(playerId);
        mjRequest.setMsg(mjMsg);

        return mjRequest;
    }

    public void entryHall(){
        BaseRequest baseRequest = new BaseRequest();
        baseRequest.setGameType(0);
        baseRequest.setMsgType(MsgTypeEnum.entryHall.msgType);
        baseRequest.setToken(token);

        socket.sendMsg(baseRequest);
    }

    public void createRoom(){
        MjRequest mjRequest = createRequest(MsgTypeEnum.createRoom);
        mjRequest.getMsg().setPayType(1);
        mjRequest.getMsg().setTotalGames(8);
        mjRequest.getMsg().setIsChiPai(1);
        mjRequest.getMsg().setNoBaiDaCanQiangGang(1);
        mjRequest.getMsg().setNoBaiDaCanZhuaChong(1);
        mjRequest.getMsg().setModel(8);
        mjRequest.getMsg().setHuButtomScore(2);
        mjRequest.getMsg().setEachFlowerScore(1);
        mjRequest.getMsg().setHuScoreLimit(50);
        mjRequest.getMsg().setIsFeiCangyin(1);
        socket.sendMsg(mjRequest);
    }

    public void entryRoom(int roomId){
        MjRequest mjRequest = createRequest(MsgTypeEnum.entryRoom);
        mjRequest.getMsg().setRoomId(roomId);
        socket.sendMsg(mjRequest);

    }

    public void playerReady(){
        MjRequest mjRequest = createRequest(MsgTypeEnum.ready);
        mjRequest.getMsg().setRoomId(roomId);
        socket.sendMsg(mjRequest);
    }

    private int chooseCard(){
        for (int i=1; i<cardList.size()-1;i++){
            if ((cardList.get(i) - cardList.get(i-1) > 1) &&
                    (cardList.get(i+1) - cardList.get(i) > 1) ){
                return i;
            }
        }
//        System.out.println(position + "--" + "随机了");
        return random.nextInt(cardList.size());
    }

    void chuPai(){
        MjRequest mjRequest = createRequest(MsgTypeEnum.chuPai);
        int cardIndex = chooseCard();
        mjRequest.getMsg().setCardIndex(cardList.get(cardIndex));
        System.out.println(position + ">>" + cardList.get(cardIndex));
        socket.sendMsg(mjRequest);
    }
    
    void chi(String chiCards){
        MjRequest mjRequest = createRequest(MsgTypeEnum.chi);
        mjRequest.getMsg().setChiCards(chiCards);
        socket.sendMsg(mjRequest);
    }

    void peng(String card){
        MjRequest mjRequest = createRequest(MsgTypeEnum.peng);
        mjRequest.getMsg().setPengCards(card);
        pengList.add(card+","+card+","+card);
        socket.sendMsg(mjRequest);
    }

    void mingGang(String card) {
        MjRequest mjRequest = createRequest(MsgTypeEnum.mingGang);
        mjRequest.getMsg().setGangCards(card);
        pengList.remove(card+","+card+","+card);
        pengList.add(card+","+card+","+card + ","+card);
        socket.sendMsg(mjRequest);
    }

    void anGang(String card) {
        MjRequest mjRequest = createRequest(MsgTypeEnum.anGang);
        mjRequest.getMsg().setGangCards(card);
        pengList.remove(card+","+card+","+card);
        pengList.add(card+","+card+","+card + ","+card);
        socket.sendMsg(mjRequest);
    }

    void qi(){
        MjRequest mjRequest = createRequest(MsgTypeEnum.pass);
        socket.sendMsg(mjRequest);
    }

    void hu(){
        MjRequest mjRequest = createRequest(MsgTypeEnum.huPai);
        socket.sendMsg(mjRequest);
    }

    void close(){
        gameOver = true;
        socket.close();
    }

    private void generateToken() throws Exception {
        String ret = HttpClientUtils.get(HTTP_URL + "/game/login");
        JSONObject jsonObject = JSON.parseObject(ret);
        token = jsonObject.getJSONObject("data").getString("token");
        playerId = jsonObject.getJSONObject("data").getInteger("playerId");
    }

    public void addRobot() throws Exception {
        String ret = HttpClientUtils.get(HTTP_URL + "/addRobot?roomId=" + roomId);
        System.out.println(ret);
    }

    public String getToken() {
        return token;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public List<Integer> getCardList() {
        return cardList;
    }

    public void setCardList(List<Integer> cardList) {
        this.cardList = cardList;
    }

    public List<Integer> getCueCardList() {
        return cueCardList;
    }

    public void setCueCardList(List<Integer> cueCardList) {
        this.cueCardList = cueCardList;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public Boolean getGameOver() {
        return gameOver;
    }

    public void setGameOver(Boolean gameOver) {
        this.gameOver = gameOver;
    }
}
