/*
 * This file is part of HuskTowns by William278. Do not redistribute!
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  All rights reserved.
 *
 *  This source code is provided as reference to licensed individuals that have purchased the HuskTowns
 *  plugin once from any of the official sources it is provided. The availability of this code does
 *  not grant you the rights to modify, re-distribute, compile or redistribute this source code or
 *  "plugin" outside this intended purpose. This license does not cover libraries developed by third
 *  parties that are utilised in the plugin.
 */

package net.william278.husktowns.cache;

import net.william278.husktowns.HuskTowns;

/**
 * Legacy enumeration of cache status, identifying the current status of a cache
 *
 * @deprecated see {@link HuskTowns#isLoaded()} instead
 */
@Deprecated(since = "2.0")
public enum CacheStatus {
    /**
     * The cache has not been loaded yet
     */
    UNINITIALIZED,
    /**
     * The cache is in the process of being loaded from the database
     */
    UPDATING,
    /**
     * The cache is ready and has been loaded from the database
     */
    LOADED,
    /**
     * The cache initialization has failed and reached an error state
     */
    ERROR
}
