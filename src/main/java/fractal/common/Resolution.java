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
    _1280X1024("1280 x 1024", 1280, 1024),

    HD("HD", 1920, 1080),
    HD_DUEL("HD Dual Monitor", HD.width*2, HD.height),
    HD_TRIPLE("HD Triple Monitor", HD.width*3, HD.height),

    _4k("4K", HD.width*2, HD.height*2),
    _4k_DUEL("4K Dual Monitor", _4k.width*2, _4k.height),
    _4K_TRIPLE("4K Triple Monitor", _4k.width*3, _4k.height),

    _8k("8K", _4k.width*2, _4k.height*2),
    _8k_DUEL("8K Dual Monitor", _8k.width*2, _8k.height),
    _8_TRIPLE("8K Triple Monitor", _8k.width*3, _8k.height);
    
    private String displayName;
    private int width;
    private int height;
    
    Resolution(String displayName, int width, int height) {
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
