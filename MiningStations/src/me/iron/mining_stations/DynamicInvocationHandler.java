package me.iron.mining_stations;

import org.schema.game.common.controller.SegmentController;
import org.schema.schine.network.objects.Sendable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 15.07.2021
 * TIME: 17:31
 * testing invokation reflection
 */
public class DynamicInvocationHandler implements InvocationHandler {
    private Sendable sc;
    public DynamicInvocationHandler(Sendable sc) {
        this.sc = sc;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ChatUI.sendAll("invoked method: " + method.getName());
        //methode ausführen und ergebnis returnen
        Object result = null;
        try {
            result = method.invoke(sc, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        System.out.println("invocation result: " + result);
        return result;
    }
}
