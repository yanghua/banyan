package com.freedom.messagebus.client.model;

/**
 * the model of rule.xml's element
 */
public class RuleModel extends BaseModel {

    private String ruleName;
    private String rulePattern;

    public RuleModel() {
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRulePattern() {
        return rulePattern;
    }

    public void setRulePattern(String rulePattern) {
        this.rulePattern = rulePattern;
    }

    @Override
    public String toString() {
        return "Rule{" +
            "ruleName='" + ruleName + '\'' +
            ", rulePattern='" + rulePattern + '\'' +
            "} " + super.toString();
    }
}
