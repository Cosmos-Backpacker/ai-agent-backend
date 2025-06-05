package com.cosmos.aiagent.agent;

import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent {


    /**
     * 抽象方法思考
     *
     * @return true: 思考完成，false: 思考中
     */
    public abstract Boolean think();


    /**
     * 执行方法
     *
     * @return 执行结果
     */
    public abstract String act();

    @Override
    public String step() {
        try {
            // 先思考判断是否需要执行
            Boolean shouldAct = think();

            if (!shouldAct) {
                return "思考完成，无需行动";
            }
            return act();

        } catch (Exception e) {
            e.printStackTrace();
            return "步骤运行出错" + e.getMessage();
        }

    }
}
