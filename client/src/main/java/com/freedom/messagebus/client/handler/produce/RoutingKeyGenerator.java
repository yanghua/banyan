//package com.freedom.messagebus.client.handler.produce;
//
//import com.freedom.messagebus.client.MessageContext;
//import com.freedom.messagebus.client.handler.AbstractHandler;
//import com.freedom.messagebus.client.handler.IHandlerChain;
//import com.freedom.messagebus.client.model.RuleModel;
//import com.freedom.messagebus.common.model.Node;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Map;
//
///**
// * the routing-key generator
// */
//@Deprecated
//public class RoutingKeyGenerator extends AbstractHandler {
//
//    private static final Log logger = LogFactory.getLog(RoutingKeyGenerator.class);
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
//        Map<String, Node> routingKeyRules = context.getConfigManager().getRoutingKeyRules();
//        String routingKey = "";
//        String pattern = routingKeyRules.get("business").getRoutingKey();
//        String ruleValue = context.getRuleValue();
//        if (ruleValue.isEmpty()) ruleValue = "#";
//        routingKey = pattern + ruleValue;
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("[handle] routing key : " + routingKey);
//        }
//
//        context.setRuleValue(routingKey);
//
//        chain.handle(context);
//    }
//}
