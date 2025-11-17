package com.gabriel.soares.Function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

public class WeeklyReportFunction {

    @FunctionName("weekly-report")
    public void run(
            @TimerTrigger(
                    name = "timerInfo",
                    schedule = "0/10 * * * * *" // a cada 10 segundos
            ) String timerInfo,
            final ExecutionContext context
    ) {
        System.out.println("PRINT: Execução de teste a cada 10 segundos antes");
        context.getLogger().info("Execução de teste a cada 10 segundos");
        System.out.println("PRINT: Execução de teste a cada 10 segundos depois");

    }
}
