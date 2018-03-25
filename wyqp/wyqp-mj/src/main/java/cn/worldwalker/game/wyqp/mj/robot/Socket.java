package cn.worldwalker.game.wyqp.mj.robot;

import cn.worldwalker.game.wyqp.common.domain.base.BaseRequest;
import cn.worldwalker.game.wyqp.common.enums.MsgTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjOperationEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class Socket extends WebSocketClient implements Runnable {

    private Client client;

    public Socket(URI serverUri, Client client) {
        super(serverUri);
        this.client = client;
    }

    public void sendMsg(BaseRequest mjRequest) {
        String type = MsgTypeEnum.getMsgTypeEnumByType(mjRequest.getMsgType()).desc;
//        type = "";
        String msg = JSON.toJSONString(mjRequest);
//        System.out.println(client.getPosition() + "--[>>>>" + type + "]: " + msg);
        send(msg);
    }

    public List<Integer> cardValue(List<Integer> cardList) {
        List<Integer> valueList = new ArrayList<>(cardList.size());
        for (Integer index : cardList) {
            valueList.add(index >> 2);
        }
        return valueList;
    }

    @Override
    public void onOpen(ServerHandshake arg0) {
//        System.out.println("打开链接");
    }

    private void refreshCardList(JSONObject jsonData){
        JSONArray jsonArray = jsonData.getJSONArray("handCardList");
        if (jsonArray != null && !jsonArray.isEmpty()){
            List<Integer> cardList = new ArrayList<>(jsonArray.size());
            for (Object o : jsonArray) {
                cardList.add((Integer) o);
            }
            client.setCardList(cardList);
//            System.out.println(client.getPosition() + "--" + client.getCardList() +
//                    " -- with:" + client.getPengList());
        }
    }
    @Override
    public void onMessage(String arg0) {
        JSONObject jsonObject = JSON.parseObject(arg0);
        Integer msgType = jsonObject.getInteger("msgType");
        JSONObject jsonData = jsonObject.getJSONObject("data");
        String type = MsgTypeEnum.getMsgTypeEnumByType(msgType).desc;
        if (!Integer.valueOf(0).equals(jsonObject.getInteger("code"))){
            System.out.println(client.getPosition() + "(" + client.getPlayerId() +  ")--[<<<<" + type + "]: " + arg0);
        }
//        type = "";
        if (msgType!=220 && msgType != 211 && msgType != 210){
            System.out.println(client.getPosition() + "(" + client.getPlayerId() +  ")--[<<<<" + type + "]: " + arg0);
        }
        if (jsonData != null) {
            Integer curPlayerId = jsonData.getInteger("curPlayerId");
            switch (msgType) {
                case 209:
                    client.setGameOver(false);
                    refreshCardList(jsonData);
                    break;
                case 211:
                    refreshCardList(jsonData);
                    break;
                case 210:
                    System.out.println(client.getPosition() + "<<" + jsonData.getString("moPaiAddFlower"));
                    refreshCardList(jsonData);
                    break;
                case 220:
                    if (Integer.valueOf(0).equals(jsonData.getInteger("remaindCardNum"))){
                        System.out.println("game Over");
//                        client.close();
                    }
                    break;
                case 24:
                    client.setGameOver(true);
                    client.playerReady();
                    System.out.println(client.getPosition() + "(" + client.getPlayerId() +  ")--[<<<<" + type + "]: " + arg0);
//                    client.close();
                    break;
                case 25:
                    client.setGameOver(true);
                    System.out.println(client.getPosition() + "(" + client.getPlayerId() +  ")--[<<<<" + type + "]: " + arg0);
                    client.close();
                    break;
                case 213:
                	refreshCardList(jsonData);

            }
            if (!client.getGameOver() && client.getPlayerId().equals(curPlayerId)
                    && msgType != 214 && msgType != 215) {
                JSONObject operationMap = jsonData.getJSONObject("operations");
                if (operationMap != null) {
                    System.out.println(client.getPosition() + "--operations:" + operationMap);
                    if (operationMap.size() > 1){
                        System.out.println(operationMap);
                    }

                    /*
                    for (int i=5; i>1; i--){
                        String openrationValue = (String) operationMap.get(String.valueOf(i));
                        if (openrationValue != null){

                        }

                    }
                   */

                    for (MjOperationEnum mjOperationEnum: MjOperationEnum.values()) {
                        String operationValue = (String) operationMap.get(String.valueOf(mjOperationEnum.type));
                        if ( operationValue != null){
                            switch (mjOperationEnum){
                                case hu:
                                    client.hu();
                                    break;
                                case mingGang:
                                    client.mingGang(operationValue);
                                    break;
                                case anGang:
                                    client.anGang(operationValue);
                                    break;
                                case peng:
                                    client.peng(operationValue);
                                    break;
                                case chi:
                                    client.chi(operationValue.split("_")[0]);
                                    break;
                            }
                            break;

                        }
                    }
                } else {
                    client.chuPai();
                }
            }

        }
        if (msgType == 5) {
            Integer roomId = jsonObject.getJSONObject("data").getInteger("roomId");
            client.setRoomId(roomId);
        } else if (msgType == 8) {
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("cardList");
            List<Integer> cardList = new ArrayList<>(jsonArray.size());
            for (Object o : jsonArray) {
                cardList.add((Integer) o);
            }
            client.setCardList(cardList);
            System.out.println(client.getPlayerId() + ":allCardList:" + cardValue(cardList));
        } else if (msgType == 403) {
            Integer landlordId = jsonObject.getJSONObject("data").getInteger("landlord");
            if (landlordId.equals(client.getPlayerId())) {
                JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("restCardList");
                List<Integer> cardList = client.getCardList();
                for (Object o : jsonArray) {
                    cardList.add((Integer) o);
                }
                System.out.println(client.getPlayerId() + ":allCardList:" + cardValue(cardList));
            }

        } else if (msgType == 406) {
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("cueCardList");
            if (jsonArray == null || jsonArray.isEmpty()) {
                System.out.println(client.getPlayerId() + ":pass:");
                client.setCueCardList(Collections.<Integer>emptyList());
            } else {
                List<Integer> cardList = new ArrayList<>(jsonArray.size());
                for (Object o : jsonArray) {
                    cardList.add((Integer) o);
                }
                client.setCueCardList(cardList);
                System.out.println(client.getPlayerId() + ":cueCardList:" + cardValue(cardList)
                        + "  index:" + cardList);
            }
        } else if (msgType == 24) {
            client.setGameOver(true);
            System.out.println(jsonObject);
        }
    }

    @Override
    public void onError(Exception arg0) {
        arg0.printStackTrace();
        System.out.println("发生错误已关闭");
    }

    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
        System.out.println(client.getToken().substring(0, 4) + ": 链接已关闭");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        try {
            System.out.println(new String(bytes.array(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
