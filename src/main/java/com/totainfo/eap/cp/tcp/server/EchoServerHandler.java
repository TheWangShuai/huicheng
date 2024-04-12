
package com.totainfo.eap.cp.tcp.server;



import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.dao.impl.LotDao;
import com.totainfo.eap.cp.dao.impl.StateDao;
import com.totainfo.eap.cp.entity.DieCountInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.GPIBHandler;
import com.totainfo.eap.cp.trx.client.EAPRepCurModel.EAPRepCurModelO;
import com.totainfo.eap.cp.trx.client.EAPReportDieInfo.DieInfoOA;
import com.totainfo.eap.cp.trx.gpib.GBIPWaferEndReport.GPIBWaferEndReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBDeviceNameReport.GPIBDeviceNameReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotEndReport.GPIBLotEndReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotStartReport.GPIBLotStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportI;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOC;
import com.totainfo.eap.cp.util.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.aspectj.weaver.ast.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;
import static io.netty.util.CharsetUtil.*;


/***
 * 服务端自定义业务处理handler
 *
 * /*NIO 使用较少的线程可以处理许多的连接 减少了内存管理
 *  * Netty 对 NIO的api进行了封装 使其的性能又得到了提升
 *  * nitty主要针对tcp协议下，面向clients端的高并发应用
 *  *
 *  * JAVA共支持3种网络编程的模型：bio同步并阻塞（服务器实现一个连接一个线程），
 *  * nio（同步非阻塞（服务器实现模式为一个线程处理多个请求），
 *  * aio（异步非阻塞）先由操作系统完成后才通知服务器程序启动线程去处理
 *  *
 *  * NIO的三大核心部分：channel（通道），buffer（缓冲区） selector（选择器）*
 *  *
 *  * buffer 缓存数组，就是一个内存块，底层用数组实现，与channal进行数据的读写，数据的读取写入通过buffer
 *  * ，nio的buffer可以读也可以写，需要flip方法切换
 *  *
 *  * channel 通信通道，每个客户端都会建立一个channel通道，客户端直接同channal进行通信，当客户端发消息时，
 *  * 消息就流通到channel里面，本地程序需要将channel里面的数据存放到buffer里面，才可以查看到本地需要发送的消息
 *  * ，先把消息存在buffer里面，再将buffer里面的数据放入channel，数据就流通到了客户端
 *  *
 *  * nio可以实现一个线程处理多个客户端通信，其关键就是selector，一个selector就对应一个线程
 *  *
 *  * /
 */

@Component
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(EchoServerHandler.class);
    //ChannelHandlerContext代表了channelhandler和channelpipeline之间的关联
    private Map<String, ChannelHandlerContext> socketMap = new ConcurrentHashMap<>();

    @Resource
    private ApplicationContext context;

    @Resource
    private StateDao stateDao;

    @Resource
    private LotDao lotDao;


    private Map<String, String> waferInfoMap = new ConcurrentHashMap<>();

    private static Queue<String> slotMapQueue = new LinkedList<String>();

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        String evtNo = UUID.randomUUID().toString();
        String remoteIp = ctx.channel().remoteAddress().toString().split(":")[0].substring(1);
        LogUtils.info("GPIB："+ remoteIp + " 连接成功。");
        EAPRepCurModelO eapRepCurModelO = new EAPRepCurModelO();
        eapRepCurModelO.setRtnCode("0000000");
        eapRepCurModelO.setRtnMesg("SUCCESS");
        eapRepCurModelO.setState("0");
        ClientHandler.sendGPIBState(evtNo,eapRepCurModelO);
        LogUtils.gpib("GPIB："+ remoteIp + " 连接成功。");
        socketMap.put("GPIB", ctx);
    }
         /**
         * 对每一个传入的消息都要调用；
         * @param ctx
         * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        StateInfo stateInfo;
        if(msg == null){
            return;
        }
        ByteBuf in = (ByteBuf) msg;
        String message = in.toString(UTF_8);
        LogUtils.info("GPIB回复的原文:["+ message +"]");
        LogUtils.gpib("GPIB回复的原文1:["+ msg +"]");
        LogUtils.gpib("GPIB回复的原文2:["+ message +"]");
        if(StringUtils.isEmpty(message)){
            LogUtils.info("GPIB回复超时");
            LogUtils.gpib("GPIB回复超时");
            return;
        }
            message = message.replaceAll("\\r","")
                .replaceAll("\\n","")
                .replaceAll(" ","")
                .replaceAll("=","")
                .replaceAll("RECEIVED","")
                .replaceAll("MSTRCVED","")
                .replaceAll("\"","")
                .replaceAll("SPERCVED","");
        LogUtils.info("EAP解析完成后的GPIB回复的数据为："+ message +"]");
        String[] strs = message.split("\\[");
        String evtNo = GUIDGenerator.javaGUID();
        EapBaseService eapBaseService;
        for(String item:strs){

            if(item.startsWith("sl") && item.length() > 2){ //DeviceName
                GPIBDeviceNameReportI gpibDeviceNameReportI = new GPIBDeviceNameReportI();
                gpibDeviceNameReportI.setDeviceName(item.substring(3).trim());
                eapBaseService = (EapBaseService) context.getBean("deviceNameReport");
                eapBaseService.subMainProc(evtNo, JacksonUtils.object2String(gpibDeviceNameReportI));
            }else if(item.startsWith("E") && item.length() == 1){  //AlarmCode
                String alarmCode = item.substring(1).trim();
                String key = String.format("EQPT:%s:ALARMCODE", equipmentNo);
                AsyncUtils.setResponse(key, alarmCode);
            }else if(item.startsWith("e") && item.length() == 1){  //AlarmMessage
                String alarmMesg = item.substring(1).trim();
                String key = String.format("EQPT:%s:ALARMMESG", equipmentNo);
                AsyncUtils.setResponse(key, alarmMesg);
            } else if(item.startsWith("V") && item.length() == 1){
                stateInfo = stateDao.getStateInfo();
                if (Integer.parseInt(stateInfo.getStep()) >= 9 ) {
                    EAPReqLotInfoOB clientLotInfo = lotDao.getClientLotInfo();
                    LotInfo lotInfo = lotDao.getCurLotInfo();
                    String sampleValue = "";
                    for (EAPReqLotInfoOB eapReqLotInfoOB : lotInfo.getParamList1()) {
                        if ("Sample".equals(eapReqLotInfoOB.getParamName())){
                            sampleValue = eapReqLotInfoOB.getParamValue();
                        }
                    }
                    EAPReqLotInfoOC eapReqLotInfoOC;
                    EAPReqLotInfoOC eapReqLotInfoOCClient;
                    if (slotMapQueue.isEmpty()){
                        if (clientLotInfo == null){
                            eapReqLotInfoOC = JacksonUtils.string2Object(sampleValue, EAPReqLotInfoOC.class);
                            String[] split = eapReqLotInfoOC.getDatas().split(",");
                            slotMapQueue.addAll(Arrays.asList(split));
                        }else{
                            eapReqLotInfoOCClient = JacksonUtils.string2Object(clientLotInfo.getParamValue(), EAPReqLotInfoOC.class);
                            if ("2".equals(eapReqLotInfoOCClient.getType())){
                                String[] split = eapReqLotInfoOCClient.getDatas().split(",");
                                slotMapQueue.addAll(Arrays.asList(split));
                            }else{
                                eapReqLotInfoOC = JacksonUtils.string2Object(sampleValue, EAPReqLotInfoOC.class);
                                String datas = eapReqLotInfoOC.getDatas();
                                String[] split = datas.split(",");
                                int index = 0;
                                for (int i = 0; i < split.length; i++) {
                                    if (eapReqLotInfoOCClient.getDatas().equals(split[i])){
                                        index = i;
                                        break;
                                    }
                                }
                                String [] splitsNew = new String[split.length - index];
                                System.arraycopy(split,index,splitsNew,0,split.length - index);
                                slotMapQueue.addAll(Arrays.asList(splitsNew));
                            }
                        }
                    }
                    LogUtils.info("GPIB上报lotStart，进入此方法----------");
                    String logNo = item.substring(1).trim();
                    GPIBLotStartReportI gpibLotStartReportI = new GPIBLotStartReportI();
                    gpibLotStartReportI.setLotNo(logNo);
                    eapBaseService = (EapBaseService) context.getBean("lotStartReport");
                    eapBaseService.subMainProc(evtNo, JacksonUtils.object2String(gpibLotStartReportI));
                }
            }else if(item.startsWith("ku") && item.length() >2){
                String siteNum = item.substring(11,12);
                String notchDirection = item.substring(15,18);
                waferInfoMap.put("siteNum", siteNum);
                waferInfoMap.put("notchDirection", notchDirection);
            }else if(item.startsWith("b") && item.length() > 10){
                stateInfo = stateDao.getStateInfo();
                if (Integer.parseInt(stateInfo.getStep()) >= 9 ) {
                    //  GPIB上报带刻号的数据长度大于10
                    LogUtils.info("GPIB上报waferStart，进入此方法----------");
                    //当前WaferID
                    String curWaferId = item.substring(1).trim();
                    waferInfoMap.put("waferId",curWaferId);
                    GPIBWaferStartReportI gpibWaferStartReportI = new GPIBWaferStartReportI();
                    gpibWaferStartReportI.setWaferId(curWaferId);
                    eapBaseService = (EapBaseService) context.getBean("waferStartReport");
                    eapBaseService.subMainProc(evtNo, JacksonUtils.object2String(gpibWaferStartReportI));
                }
            } else if (item.startsWith("P") && item.length() == 1) {
                stateInfo = stateDao.getStateInfo();
                if (Integer.parseInt(stateInfo.getStep()) >= 9 ) {
                    LogUtils.info("GPIB上报waferEnd，进入此方法----------");
                    String curWaferId = waferInfoMap.get("waferId");
                    GPIBWaferEndReportI gpibWaferEndReportI = new GPIBWaferEndReportI();
                    gpibWaferEndReportI.setWaferId(curWaferId);
                    eapBaseService = (EapBaseService) context.getBean("waferEndReport");
                    eapBaseService.subMainProc(evtNo, JacksonUtils.object2String(gpibWaferEndReportI));
                }
            } else if(item.startsWith("O") && item.length() == 1){

            }else if(item.startsWith("Q") && item.length() == 1){
                String startCoordinate = item.substring(1).trim();
                waferInfoMap.put("startCoorDinate", startCoordinate);
            }else if(item.startsWith("M") && item.length() == 1){
                stateInfo = stateDao.getStateInfo();
                if (Integer.parseInt(stateInfo.getStep()) >= 9 ) {
                    String result = item.substring(1).trim();
                    waferInfoMap.put("result", result);
                    eapBaseService = (EapBaseService) context.getBean("dieInfoReport");
                    String s = JacksonUtils.object2String(waferInfoMap);
                    eapBaseService.subMainProc(evtNo, JacksonUtils.object2String(waferInfoMap));
                }
            }else if(item.startsWith("^") && item.length() == 1){
                stateInfo = stateDao.getStateInfo();
                DieCountInfo dieCountInfo = lotDao.getDieCount();
                List<DieInfoOA> dieInfoOAS = dieCountInfo.getDieInfoOAS();
                LogUtils.info("当前制程步骤为：" + stateInfo.getStep());
                if (!dieInfoOAS.isEmpty() && slotMapQueue.size() != dieInfoOAS.size()) {
                    ClientHandler.sendMessage(evtNo, false, 2, "LotEnd异常结束！" );
                }
                if (Integer.parseInt(stateInfo.getStep()) < 9 ){
                    ClientHandler.sendMessage(evtNo, true, 1, "GPIB回复数据格式失败，回复的数据为：" + message);
                    GPIBHandler.changeMode("++device");
                    return;
                }
                LogUtils.info("GPIB上报制程结束，进入此方法----------");
                GPIBLotEndReportI endReportI = new GPIBLotEndReportI();
                eapBaseService = (EapBaseService) context.getBean("lotEndReport");
                eapBaseService.subMainProc(evtNo, JacksonUtils.object2String(endReportI));
            }else if(item.contains("++master")){
                ClientHandler.sendMessage(evtNo, false, 2, "GPIB切换主机模式成功！" );
            }
        }
    }
    /**
     * 通知ChannelInboundHandler最后一次对channelRead()的调用时当前批量读取中的的最后一条消息。
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

     /**
     * 在读取操作期间，有异常抛出时会调用。
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtils.error("Socket 通信异常", cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String evtNo = UUID.randomUUID().toString();
        super.channelInactive(ctx);
        socketMap.remove("GPIB");
        EAPRepCurModelO eapRepCurModelO = new EAPRepCurModelO();
        eapRepCurModelO.setRtnCode("0000000");
        eapRepCurModelO.setRtnMesg("SUCCESS");
        eapRepCurModelO.setState("2");
        ClientHandler.sendGPIBState(evtNo,eapRepCurModelO);
        LogUtils.info("GPIB断开连接。");
        LogUtils.gpib("GPIB断开连接。");
    }

    public void send(String eqptId, String message){
        try {
            Thread.sleep(2000); // 暂停2秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ChannelHandlerContext ctrx = socketMap.get(eqptId);
        if(ctrx == null){
            LogUtils.error("设备:[" + eqptId + "]没有连接，请确认");
            return;
        }
        String parseMessage =  message + System.lineSeparator();
        ByteBuf byteBuf = Unpooled.copiedBuffer((message+System.lineSeparator()).getBytes());
        LogUtils.info("EAP->GPIB-send:["+ parseMessage + "]");
        LogUtils.gpib("EAP->GPIB-send:["+ parseMessage + "]");
        ctrx.writeAndFlush(byteBuf);
    }

    public String sendForReply(String eqptId, String message) {
        try {
            Thread.sleep(2000); // 暂停2秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ChannelHandlerContext ctx = socketMap.get(eqptId);
        if(ctx == null){
            LogUtils.error("设备:[" + eqptId + "]没有连接，请确认");
        }
        String newMessage =  message + System.lineSeparator();
        ByteBuf byteBuf = Unpooled.copiedBuffer((message + System.lineSeparator()).getBytes());
        String reply = byteBuf.toString(StandardCharsets.UTF_8);
        LogUtils.info("EAP->GPIB-sendForReplay:["+ newMessage + "]");
        LogUtils.gpib("EAP->GPIB-sendForReplay:["+ newMessage + "]");
        ctx.writeAndFlush(byteBuf);
        return reply;
    }
}
