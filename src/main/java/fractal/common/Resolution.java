/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

/**
 *
 * @author Lloyd
 */
public enum Resolution {
    _640X480("640 x 480", 640, 480),
    _800X600("800 x 600", 800, 600),
    _1280X1024("1280 x 1024", 640, 480),
    HD("HD", 1920, 1080),
    HD_DUEL("HD Dual Monitor", 3840, 1080),
    HD_TRIPLE("HD Triple Monitor", 5760, 1080),
    _4k("4K", 3840, 2160),
    _4k_DUEL("4K Dual Monitor", 7680, 2160),
    _4K_TRIPLE("4K Triple Monitor", 11520, 2160),
    _8k("8K", 7680, 4320),
    _8k_DUEL("8K Dual Monitor", 15360, 4320),
    _8_TRIPLE("8K Triple Monitor", 23040, 4320);
    
    private String displayName;
    private int width;
    private int height;
    
    private Resolution(String displayName, int width, int height) {
        this.displayName = displayName;
        this.width = width;
        this.height = height;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    
    
}
