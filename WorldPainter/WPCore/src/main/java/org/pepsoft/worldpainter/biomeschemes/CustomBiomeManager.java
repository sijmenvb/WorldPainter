/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.biomeschemes;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static org.pepsoft.worldpainter.biomeschemes.Minecraft1_17Biomes.FIRST_UNALLOCATED_ID;

/**
 * Coordinates loading, saving and editing of custom biomes.
 * 
 * @author pepijn
 */
public class CustomBiomeManager {
    public synchronized List<CustomBiome> getCustomBiomes() {
        return customBiomes;
    }

    public synchronized void setCustomBiomes(List<CustomBiome> customBiomes) {
        final List<CustomBiome> oldCustomBiomes = new ArrayList<>(this.customBiomes);
        this.customBiomes.clear();
        this.customBiomes.addAll(customBiomes);
        for (CustomBiome customBiome: oldCustomBiomes) {
            listeners.stream()
                    .map(Reference::get)
                    .filter(Objects::nonNull)
                    .forEach(listener -> listener.customBiomeRemoved(customBiome));
        }
        for (CustomBiome customBiome: customBiomes) {
            listeners.stream()
                    .map(Reference::get)
                    .filter(Objects::nonNull)
                    .forEach(listener -> listener.customBiomeAdded(customBiome));
        }
    }

    public synchronized void clearCustomBiomes() {
        setCustomBiomes(emptyList());
    }

    public synchronized int getNextId() {
        outer:
        for (int i = FIRST_UNALLOCATED_ID; i < 256; i++) {
            for (CustomBiome customBiome : customBiomes) {
                if (customBiome.getId() == i) {
                    continue outer;
                }
            }
            if (isBiomePresent(i)) {
                continue;
            }
            return i;
        }
        return - 1;
    }

    public synchronized boolean addCustomBiome(Window parent, CustomBiome customBiome) {
        if (isBiomePresent(customBiome.getId())) {
            if (parent != null) {
                JOptionPane.showMessageDialog(parent, "The specified ID (" + customBiome.getId() + ") is already a regular biome (named " + Minecraft1_17Biomes.BIOME_NAMES[customBiome.getId()] + ")", "ID Already In Use", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
        for (CustomBiome existingCustomBiome: customBiomes) {
            if (existingCustomBiome.getId() == customBiome.getId()) {
                if (parent != null) {
                    JOptionPane.showMessageDialog(parent, "You already configured a custom biome with that ID (named " + existingCustomBiome.getName() + ")", "ID Already In Use", JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }
        }
        customBiomes.add(customBiome);
        listeners.stream()
                .map(Reference::get)
                .filter(Objects::nonNull)
                .forEach(listener -> listener.customBiomeAdded(customBiome));
        return true;
    }
    
    /**
     * Indicates that some aspect of a custom biome (other than the ID, which is
     * not allowed to change) has changed.
     * 
     * @param customBiome The custom biome that has been modified.
     */
    public synchronized void editCustomBiome(CustomBiome customBiome) {
        for (CustomBiome existingCustomBiome: customBiomes) {
            if (existingCustomBiome.getId() == customBiome.getId()) {
                listeners.stream()
                        .map(Reference::get)
                        .filter(Objects::nonNull)
                        .forEach(listener -> listener.customBiomeChanged(customBiome));
                return;
            }
        }
        throw new IllegalArgumentException("There is no custom biome installed with ID " + customBiome.getId());
    }
    
    /**
     * Removes a custom biome.
     * 
     * @param customBiome The custom biome to remove.
     */
    public synchronized void removeCustomBiome(CustomBiome customBiome) {
        for (Iterator<CustomBiome> i = customBiomes.iterator(); i.hasNext(); ) {
            CustomBiome existingCustomBiome = i.next();
            if (existingCustomBiome.getId() == customBiome.getId()) {
                i.remove();
                listeners.stream()
                        .map(Reference::get)
                        .filter(Objects::nonNull)
                        .forEach(listener -> listener.customBiomeRemoved(customBiome));
                return;
            }
        }
        throw new IllegalArgumentException("There is no custom biome installed with ID " + customBiome.getId());
    }
    
    public synchronized void addListener(CustomBiomeListener listener) {
        // No idea how this is possible, but it has happened in the wild that one of the entries in listeners was null.
        // May have been a concurrency issue, but just to be sure:
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        listeners.add(new WeakReference<>(listener));
    }
    
    public synchronized void removeListener(CustomBiomeListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        listeners.remove(listener);
    }

    private static boolean isBiomePresent(int biome) {
        return (biome <= Minecraft1_17Biomes.HIGHEST_BIOME_ID) && (Minecraft1_17Biomes.BIOME_NAMES[biome] != null);
    }

    private final List<Reference<CustomBiomeListener>> listeners = new ArrayList<>();
    private final List<CustomBiome> customBiomes = new ArrayList<>(256);
    
    public interface CustomBiomeListener {
        void customBiomeAdded(CustomBiome customBiome);
        void customBiomeChanged(CustomBiome customBiome);
        void customBiomeRemoved(CustomBiome customBiome);
    }
}