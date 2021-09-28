package com.ahd.ad_center_flutter.Net;

/**
 * Author by GuangMingfei
 * Date on 2021/8/31.
 * Email guangmf@neusoft.com
 * Used for
 * {
 *   "appId": 0,
 *   "channel": "string",
 *   "kind": 0,
 *   "listingId": "string",
 *   "source": "string",
 *   "userId": 0,
 *   "version": "string"
 * }
 */
public class RequestUploadVideoData extends BaseRequest{
    public String kind;
    public String listingId;
    public String status;
    public String version;
}
