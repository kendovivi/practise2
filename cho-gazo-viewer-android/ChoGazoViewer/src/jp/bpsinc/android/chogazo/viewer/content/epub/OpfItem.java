
package jp.bpsinc.android.chogazo.viewer.content.epub;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import jp.bpsinc.android.chogazo.viewer.content.ContentsReader;
import jp.bpsinc.android.chogazo.viewer.content.PageItem;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.EpubParseException;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.util.StringUtil;
import jp.bpsinc.android.chogazo.viewer.util.XmlUtil;

public class OpfItem implements PageItem {
    private static final String MEDIA_TYPE_HTML = "text/html";
    private static final String MEDIA_TYPE_XHTML = "application/xhtml+xml";

    public static final String ATT_ID = "id";
    public static final String ATT_HREF = "href";
    public static final String ATT_MEDIA_TYPE = "media-type";
    public static final String ATT_FALLBACK = "fallback";
    public static final String ATT_PROPERTIES = "properties";

    private final ContentsReader mReader;
    private final String mOpfDir;
    private final String mId;
    private final String mHref;
    private final String mMediaType;
    private final String[] mProperties;
    private String[] mSpineProperties;
    private OpfItem mFallback;

    public OpfItem(ContentsReader reader, String opfDir, String id, String href, String mediaType,
            String properties) {
        mReader = reader;
        mOpfDir = opfDir;
        mId = id;
        mHref = href;
        mMediaType = mediaType;
        if (properties != null) {
            mProperties = properties.split(" ");
        } else {
            mProperties = null;
        }
    }

    @Override
    public String getKey() throws EpubParseException, ContentsOtherException {
        String uri = getImagePath(this);
        String fileName = uriToFilename(uri);
        LogUtil.v("uri: %s, filename=%s", uri, fileName);
        return fileName;
    }

    public void setSpineProperties(String properties) {
        if (properties != null) {
            mSpineProperties = properties.split(" ");
        }
    }

    public String getId() {
        return mId;
    }

    public String getHref() {
        return mHref;
    }

    public String getMediaType() {
        return mMediaType;
    }

    /**
     * HTML, XHTMLならtrueを返す
     * 
     * @return
     */
    public boolean isHtml() {
        if (mMediaType != null) {
            if (mMediaType.equals(MEDIA_TYPE_XHTML) || mMediaType.equals(MEDIA_TYPE_HTML)) {
                return true;
            }
        }
        return false;
    }

    /**
     * XHTMLならtrueを返す
     * 
     * @return
     */
    public boolean isXhtml() {
        if (mMediaType != null) {
            if (mMediaType.equals(MEDIA_TYPE_XHTML)) {
                return true;
            }
        }
        return false;
    }

    /**
     * image/jpeg, image/pngなど画像ならtrueを返す
     * 
     * @return
     */
    public boolean isImage() {
        if (mMediaType != null) {
            if (mMediaType.startsWith("image/") && !mMediaType.contains("svg")) {
                return true;
            }
        }
        return false;
    }

    /**
     * EPUBのnav要素かどうかを判定
     * 
     * @return
     */
    public boolean isNav() {
        if (mProperties != null) {
            for (String p : mProperties) {
                if (p.equals("nav")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isCoverImage() {
        if (mProperties != null) {
            for (String p : mProperties) {
                if (p.equals("cover-image")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * このページの表示位置を取得
     * 
     * @return
     */
    public PageSpread getPageSpread() {
        if (mSpineProperties != null) {
            for (String p : mSpineProperties) {
                if (p.equals(PageSpread.LEFT.getValue())) {
                    return PageSpread.LEFT;
                } else if (p.equals(PageSpread.RIGHT.getValue())) {
                    return PageSpread.RIGHT;
                } else if (p.equals(PageSpread.CENTER.getValue())) {
                    return PageSpread.CENTER;
                }
            }
        }
        return PageSpread.NONE;
    }

    public OpfItem getFallback() {
        return mFallback;
    }

    public void setFallback(OpfItem mFallback) {
        this.mFallback = mFallback;
    }

    private byte[] getFileContents(String uri) throws ContentsOtherException {
        String fileName = uriToFilename(uri);
        LogUtil.v("uri: %s, filename=%s", uri, fileName);
        return mReader.getFileContents(fileName);
    }

    private String getImagePath(OpfItem item) throws EpubParseException, ContentsOtherException {
        if (item == null) {
            return null;
        }

        if (item.isXhtml()) {
            String path;
            if ((path = getImagePathByBbook(item)) != null) {
                return path;
            }
            if ((path = getImagePathBySvg(item)) != null) {
                return path;
            }
            if ((path = getImagePathByImg(item)) != null) {
                return path;
            }
        } else if (item.isImage()) {
            return item.getHref();
        }
        return getImagePath(item.getFallback());
    }

    private String getImagePathByBbook(OpfItem item) throws EpubParseException,
            ContentsOtherException {
        try {
            XmlPullParser parser = XmlUtil.newPullParser(getFileContents(item.getHref()));
            int event = parser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if (parser.getName().equalsIgnoreCase("meta")) {
                        String name = parser.getAttributeValue(null, "name");
                        String content = parser.getAttributeValue(null, "content");
                        if (name != null && name.equals("bbook-page-image")) {
                            if (content != null && content.length() > 0) {
                                return xmlBuildPath(content, item.getHref());
                            }
                        }
                    }
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            throw new EpubParseException("epub parse error", e);
        } catch (IOException e) {
            throw new ContentsOtherException("epub parse i/o error", e);
        }
        return null;
    }

    private String getImagePathBySvg(OpfItem item) throws EpubParseException,
            ContentsOtherException {
        try {
            XmlPullParser parser = XmlUtil.newPullParser(getFileContents(item.getHref()));
            int event = parser.getEventType();

            boolean svg = false;
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if (svg && parser.getName().equalsIgnoreCase("image")) {
                        String href = parser.getAttributeValue(
                                "http://www.w3.org/1999/xlink", "href");
                        if (href != null && href.length() > 0) {
                            return xmlBuildPath(href, item.getHref());
                        }
                    } else if (parser.getName().equalsIgnoreCase("svg")) {
                        svg = true;
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if (parser.getName().equalsIgnoreCase("svg")) {
                        svg = false;
                    }
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            throw new EpubParseException("epub parse error", e);
        } catch (IOException e) {
            throw new ContentsOtherException("epub parse i/o error", e);
        }
        return null;
    }

    private String getImagePathByImg(OpfItem item) throws EpubParseException,
            ContentsOtherException {
        try {
            XmlPullParser parser = XmlUtil.newPullParser(getFileContents(item.getHref()));
            int event = parser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if (parser.getName().equalsIgnoreCase("img")) {
                        String href = parser.getAttributeValue(null, "src");
                        if (href != null && href.length() > 0) {
                            return xmlBuildPath(href, item.getHref());
                        }
                    }
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            throw new EpubParseException("epub parse error", e);
        } catch (IOException e) {
            throw new ContentsOtherException("epub parse i/o error", e);
        }
        return null;
    }

    private String xmlBuildPath(String href, String base) {
        if (base == null) {
            return href;
        }

        if (base.contains("/")) {
            base = base.substring(0, base.lastIndexOf("/"));
        } else {
            base = "";
        }

        while (href.startsWith("../")) {
            href = href.substring(3);
            if (base.contains("/")) {
                base = base.substring(0, base.lastIndexOf("/"));
            } else {
                base = "";
            }
        }

        if (base.length() == 0) {
            return href;
        } else {
            return base + "/" + href;
        }
    }

    private String uriToFilename(String uri) {
        String result = mOpfDir + StringUtil.trimUri(uri);
        LogUtil.v("%s => %s", uri, result);
        return result;
    }
}
