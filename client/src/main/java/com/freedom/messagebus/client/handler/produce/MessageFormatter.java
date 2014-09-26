//package com.freedom.messagebus.client.handler.produce;
//
//import com.freedom.messagebus.client.MessageContext;
//import com.freedom.messagebus.common.message.Message;
//import com.freedom.messagebus.client.formatter.FormatterFactory;
//import com.freedom.messagebus.client.formatter.IFormatter;
//import com.freedom.messagebus.client.handler.AbstractHandler;
//import com.freedom.messagebus.client.handler.IHandlerChain;
//import com.freedom.messagebus.client.model.MessageFormat;
//import com.freedom.messagebus.client.model.MsgBytes;
//import org.jetbrains.annotations.NotNull;
//
///**
// * the message format handler
// */
//public class MessageFormatter extends AbstractHandler {
//
//    /**
//     * the main process method all sub class must implement
//     *
//     * @param context the message context
//     * @param chain   the instance of IHandlerChain
//     */
//    @Override
//    public void handle(@NotNull MessageContext context,
//                       @NotNull IHandlerChain chain) {
////        MessageFormat msgFormat = context.getMsgFormat();
////        IFormatter formatter = FormatterFactory.getFormatter(msgFormat);
//        Message[] msgs = context.getMessages();
//
//        context.setMsgBytes(new MsgBytes[msgs.length]);
//
//        for (int i = 0; i < msgs.length; i++) {
//            byte[] bytes = formatter.format(msgs[i]);
//            MsgBytes msgBytes = new MsgBytes();
//            msgBytes.setMsgBytes(bytes);
//            context.getMsgBytes()[i] = msgBytes;
//        }
//
//        chain.handle(context);
//    }
//
//}
