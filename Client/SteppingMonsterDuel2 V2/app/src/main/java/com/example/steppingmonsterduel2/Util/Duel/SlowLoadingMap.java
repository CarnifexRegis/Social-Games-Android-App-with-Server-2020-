package com.example.steppingmonsterduel2.Util.Duel;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

//Slow loading map allows you to specify a Consumer<V> listener in case the map doesn't yet have an entry for the K key.
//when a value for that key is finally entered, all listeners waiting for that key will be called.
//tbh I probably could have solved my problem without this but it was fun.
//TODO thread safety, probably
public class SlowLoadingMap<K, V> {
    private final Map<K, V> map = new HashMap<>();
    private final Map<K, List<Consumer<V>>> listeners = new HashMap<>();

    public void put(K key, V value){
        map.put(key, value);
        if(listeners.containsKey(key)){
            for(Consumer<V> listener : listeners.get(key)){
                listener.accept(value);
            }
        }
    }
    public @Nullable V get(K key){
        return map.get(key);
    }
    public void slowGet(K key, Consumer<V> listener){
        if(map.containsKey(key)) listener.accept(map.get(key));
        else {
            if(listeners.containsKey(key)) listeners.get(key).add(listener);
            else {
                List<Consumer<V>> listenerQueue = new ArrayList<>();
                listenerQueue.add(listener);
                listeners.put(key, listenerQueue);
            }
        }
    }
    public boolean containsKey(K key){
        return map.containsKey(key);
    }
}