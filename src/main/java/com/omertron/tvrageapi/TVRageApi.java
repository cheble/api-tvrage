/*
 *      Copyright (c) 2004-2015 Stuart Boston
 *
 *      This file is part of the TVRage API.
 *
 *      TVRage API is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      TVRage API is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with TVRage API.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.omertron.tvrageapi;

import com.omertron.tvrageapi.model.Episode;
import com.omertron.tvrageapi.model.EpisodeList;
import com.omertron.tvrageapi.model.ShowInfo;
import com.omertron.tvrageapi.tools.DOMHelper;
import com.omertron.tvrageapi.tools.TVRageParser;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamj.api.common.http.SimpleHttpClientBuilder;

/**
 * TV Rage API
 *
 * @author Stuart.Boston
 *
 */
public class TVRageApi {

    private static final Logger LOG = LoggerFactory.getLogger(TVRageApi.class);
    private String apiKey = null;
    public static final String UNKNOWN = "UNKNOWN";
    private static final String API_EPISODE_INFO = "episodeinfo.php";
    private static final String API_EPISODE_LIST = "episode_list.php";
    private static final String API_SEARCH = "search.php";
    private static final String API_SHOWINFO = "showinfo.php";
    private static final String API_SITE = "http://services.tvrage.com/myfeeds/";

    /**
     * Constructor, requires the API Key
     *
     * @param apiKey
     */
    public TVRageApi(String apiKey) {
        // Use a default http client if one is not provided
        this(apiKey, new SimpleHttpClientBuilder().build());
    }

    /**
     * Constructor, requires API key and a CommonHttpClient
     *
     * @param apiKey
     * @param httpClient
     */
    public TVRageApi(String apiKey, HttpClient httpClient) {
        if (StringUtils.isBlank(apiKey)) {
            throw new UnsupportedOperationException("No API Key provided!");
        }

        this.apiKey = apiKey;
        DOMHelper.setHttpClient(httpClient);
    }

    /**
     * Get the information for a specific episode
     *
     * @param showID
     * @param seasonId
     * @param episodeId
     * @return
     * @throws com.omertron.tvrageapi.TVRageException
     */
    public Episode getEpisodeInfo(String showID, String seasonId, String episodeId) throws TVRageException {
        if (!isValidString(showID) || !isValidString(seasonId) || !isValidString(episodeId)) {
            return new Episode();
        }

        StringBuilder tvrageURL = buildURL(API_EPISODE_INFO, showID);
        // Append the Season & Episode to the URL
        tvrageURL.append("&ep=").append(seasonId);
        tvrageURL.append("x").append(episodeId);

        return TVRageParser.getEpisodeInfo(tvrageURL.toString());
    }

    /**
     * Get the episode information for all episodes for a show
     *
     * @param showID
     * @return
     * @throws com.omertron.tvrageapi.TVRageException
     */
    public EpisodeList getEpisodeList(String showID) throws TVRageException {
        if (!isValidString(showID)) {
            return new EpisodeList();
        }

        String tvrageURL = buildURL(API_EPISODE_LIST, showID).toString();
        return TVRageParser.getEpisodeList(tvrageURL);
    }

    /**
     * Search for the show using the show ID
     *
     * @param showID
     * @return ShowInfo
     * @throws com.omertron.tvrageapi.TVRageException
     */
    public ShowInfo getShowInfo(int showID) throws TVRageException {
        if (showID == 0) {
            return new ShowInfo();
        }

        String tvrageURL = buildURL(API_SHOWINFO, Integer.toString(showID)).toString();
        List<ShowInfo> showList = TVRageParser.getShowInfo(tvrageURL);
        if (showList.isEmpty()) {
            return new ShowInfo();
        } else {
            return showList.get(0);
        }
    }

    /**
     * Get the show information using the show ID
     *
     * @param showID
     * @return
     * @throws com.omertron.tvrageapi.TVRageException
     */
    public ShowInfo getShowInfo(String showID) throws TVRageException {
        int id = NumberUtils.toInt(showID, 0);
        if (id > 0) {
            return getShowInfo(id);
        } else {
            return new ShowInfo();
        }
    }

    /**
     * Search for the show using the show name
     *
     * @param showName
     * @return list of matching shows
     * @throws com.omertron.tvrageapi.TVRageException
     */
    public List<ShowInfo> searchShow(String showName) throws TVRageException {
        if (!isValidString(showName)) {
            return new ArrayList<>();
        }

        String tvrageURL = buildURL(API_SEARCH, showName).toString();
        return TVRageParser.getSearchShow(tvrageURL);
    }

    /**
     * Build the API web URL for the process
     *
     * @param urlParameter
     */
    private StringBuilder buildURL(String urlParameter, String urlData) {
        StringBuilder tvrageURL = new StringBuilder();
        tvrageURL.append(API_SITE);
        tvrageURL.append(urlParameter);
        tvrageURL.append("?key=");
        tvrageURL.append(apiKey);
        tvrageURL.append("&");

        if (urlParameter.equalsIgnoreCase(API_SEARCH)) {
            tvrageURL.append("show=").append(urlData);
        } else if (urlParameter.equalsIgnoreCase(API_SHOWINFO)) {
            tvrageURL.append("sid=").append(urlData);
        } else if (urlParameter.equalsIgnoreCase(API_EPISODE_LIST)) {
            tvrageURL.append("sid=").append(urlData);
        } else if (urlParameter.equalsIgnoreCase(API_EPISODE_INFO)) {
            tvrageURL.append("sid=").append(urlData);
            // Note this needs the season & episode appending to the url
        } else {
            return new StringBuilder(UNKNOWN);
        }

        LOG.trace("Search URL: {}", tvrageURL);
        return tvrageURL;
    }

    /**
     * Check the string passed to see if it contains a value.
     *
     * @param testString The string to test
     * @return False if the string is empty, null or UNKNOWN, True otherwise
     */
    public static boolean isValidString(String testString) {
        return StringUtils.isNotBlank(testString) && (!testString.equalsIgnoreCase(TVRageApi.UNKNOWN));
    }
}
