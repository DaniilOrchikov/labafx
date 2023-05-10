package client.labafx;

import lombok.SneakyThrows;
import utility.Command;

import java.util.Arrays;
import java.util.concurrent.RecursiveTask;

public class ValueSumCounter extends RecursiveTask<String> {

    private final Long[] array;
    private final ClientLogic clientLogic;

    public ValueSumCounter(Long[] array, ClientLogic clientLogic) {
        this.array = array;
        this.clientLogic = clientLogic;
    }

    @SneakyThrows
    @Override
    protected String compute() {
        if (array.length < 2){
            return clientLogic.communicatingWithServer(new Command(new String[]{"get", array[0].toString()}, clientLogic.userName, clientLogic.userPassword)).text() + "\n";
        }
        if (array.length == 2) {
            return clientLogic.communicatingWithServer(new Command(new String[]{"get", array[0].toString()}, clientLogic.userName, clientLogic.userPassword)).text() + "\n" + clientLogic.communicatingWithServer(new Command(new String[]{"get", array[1].toString()}, clientLogic.userName, clientLogic.userPassword)).text() + "\n";
        }
        ValueSumCounter firstHalfArrayValueSumCounter = new ValueSumCounter(Arrays.copyOfRange(array, 0, array.length / 2), clientLogic);
        ValueSumCounter secondHalfArrayValueSumCounter = new ValueSumCounter(Arrays.copyOfRange(array, array.length / 2, array.length), clientLogic);
        firstHalfArrayValueSumCounter.fork();
        secondHalfArrayValueSumCounter.fork();
        return firstHalfArrayValueSumCounter.join() + secondHalfArrayValueSumCounter.join();
    }
}
