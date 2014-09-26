//package com.freedom.messagebus.client.handler.consumer;
//
//import com.freedom.messagebus.client.MessageContext;
//import com.freedom.messagebus.client.core.message.Message;
//import com.freedom.messagebus.client.formatter.FormatterFactory;
//import com.freedom.messagebus.client.formatter.IFormatter;
//import com.freedom.messagebus.client.handler.AbstractHandler;
//import com.freedom.messagebus.client.handler.IHandlerChain;
//import org.jetbrains.annotations.NotNull;
//
///**
// * message deformat handler, it will fetch the message's format and choose
// * right formatter to extract original information
// */
//public class MessageDeFormatter extends AbstractHandler {
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
//        IFormatter formatter = FormatterFactory.getFormatter(context.getMsgFormat());
//
//        Message msg = formatter.deFormat(context.getConsumedMsgBytes());
//        context.setConsumedMsg(msg);
//
//        chain.handle(context);
//    }
//}
