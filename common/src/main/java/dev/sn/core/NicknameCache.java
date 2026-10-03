package dev.sn.core;

/**
 * Lets a nickname change drop the display name cache that {@link dev.sn.mixin.PlayerMixin} keeps.
 */
public interface NicknameCache {
    void styledNicknames$invalidateCache();
}