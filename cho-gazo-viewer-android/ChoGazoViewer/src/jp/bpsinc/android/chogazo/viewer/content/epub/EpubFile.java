
package jp.bpsinc.android.chogazo.viewer.content.epub;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;
import jp.bpsinc.android.chogazo.viewer.exception.EpubParseException;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.util.StringUtil;
import jp.bpsinc.android.chogazo.viewer.content.Book;
import jp.bpsinc.android.chogazo.viewer.content.ContentsReader;
import jp.bpsinc.android.chogazo.viewer.content.NavListItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class EpubFile extends Book {
    private static final String CONTAINER_FILENAME = "META-INF/container.xml";
    private static final int NAV_TYPE_TOC = 3;
    private static final int NAV_TYPE_LANDMARKS = 2;
    private static final int NAV_TYPE_PAGE_LIST = 1;
    private static final int NAV_TYPE_NONE = 0;
    private static final int NAV_TYPE_DEFAULT = -1;

    public class NavPoint implements NavListItem {
        public int order = -1;
        public OpfItem item = null;
        public String title = null;

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int getSinglePageIndex() {
            int size = getPageCount();
            for (int i = 0; i < size; i++) {
                if (((OpfItem) getSinglePageInfo().get(i).getCenterPageItem())
                        .getId().equals(item.getId())) {
                    return i;
                }
            }
            return 0;
        }
    }

    /** OPFファイルのパス */
    private String mOpfPath;
    /** OPFファイルのあるディレクトリパス */
    private String mOpfDir;
    /** OPFファイルに含まれるメタデータ */
    private OpfMeta mOpfMeta;
    /** ページめくり（RTL, LTR）方向 */
    protected PageDirection mPageDirection;
    /** すべてのitem要素（IDとitemのkey-value） */
    protected HashMap<String, OpfItem> mItems;

    @Override
    public void parse(ContentsReader reader) throws ContentsParseException, ContentsOtherException {
        if (reader == null || reader.isClosed()) {
            throw new ContentsOtherException("reader is not open");
        }
        mItems = new HashMap<String, OpfItem>();
        mPageDirection = PageDirection.DEFAULT;
        open(reader, null);
    }

    @Override
    protected void bibliographicInit() {
        String title = mOpfMeta.getTitle();
        String author = mOpfMeta.getAuthor();
        if (title != null) {
            mTitle = title;
        }
        if (author != null) {
            mAuthor = author;
        }
    }

    @Override
    public boolean isRtl() {
        return mPageDirection == PageDirection.RTL || mPageDirection == PageDirection.DEFAULT;
    }

    private void open(ContentsReader reader, String priorityOpfName) throws EpubParseException,
            ContentsOtherException {
        // container.xmlを読んで、OPFパスを取得
        byte[] byteContainer = reader.getFileContents(CONTAINER_FILENAME);
        if (byteContainer == null) {
            throw new ContentsOtherException("Cannot read " + CONTAINER_FILENAME);
        }
        mOpfPath = getOpfPath(StringUtil.trimBOM(new String(byteContainer)), priorityOpfName);
        if (mOpfPath == null) {
            throw new EpubParseException(
                    "Cannot resolve opf file path. Maybe this file is not a EPUB file");
        }
        mOpfDir = StringUtil.getParentPath(mOpfPath);

        // OPFを読み込む
        byte[] byteOpf = reader.getFileContents(mOpfPath);
        if (byteOpf == null) {
            throw new ContentsOtherException("Cannot read opf file.");
        }
        String opfXml = StringUtil.trimBOM(new String(byteOpf));
        mOpfMeta = OpfMeta.parseOpf(opfXml);
        if (mOpfMeta == null) {
            throw new EpubParseException("OPF MetaData is invalid");
        }
        bibliographicInit();

        if (parseOpf(reader, opfXml) == false) {
            throw new ContentsOtherException("Failed to parse OPF item/itemref");
        }
    }

    /**
     * itemに記載されているhrefは、OPFファイルからの相対パスになっているので、OPFのディレクトリ分を追加して返す
     * 
     * @param item
     * @return
     */
    protected String getFileName(OpfItem item) {
        String href = item.getHref();
        int index = href.indexOf('#');
        if (index != -1) {
            href = href.substring(0, index);
        }
        return mOpfDir + File.separator + href;
    }

    protected String getOpfDir() {
        return mOpfDir;
    }

    /**
     * OPFファイルから、itemとitemrefの要素を読み込む
     * 
     * @param opfXml
     * @return
     * @throws EpubParseException
     * @throws ContentsOtherException
     */
    protected boolean parseOpf(ContentsReader reader, String opfXml) throws EpubParseException,
            ContentsOtherException {
        ArrayList<OpfItem> spineItems = new ArrayList<OpfItem>();
        HashMap<OpfItem, String> fallbackMap = new HashMap<OpfItem, String>();
        String tocId = null;

        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(opfXml));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    String tag = parser.getName();
                    if (tag.equals("item")) {
                        String id = parser.getAttributeValue(null, OpfItem.ATT_ID);
                        String href = parser.getAttributeValue(null, OpfItem.ATT_HREF);
                        String mediaType = parser.getAttributeValue(null, OpfItem.ATT_MEDIA_TYPE);
                        String properties = parser.getAttributeValue(null, OpfItem.ATT_PROPERTIES);

                        OpfItem item = new OpfItem(
                                reader, mOpfDir, id, href, mediaType, properties);
                        mItems.put(id, item);

                        String fallback = parser.getAttributeValue(null, OpfItem.ATT_FALLBACK);
                        if (fallback != null && fallback.length() > 0) {
                            fallbackMap.put(item, fallback);
                        }
                    } else if (tag.equals("spine")) {
                        String direction =
                                parser.getAttributeValue(null, "page-progression-direction");
                        tocId = parser.getAttributeValue(null, "toc");

                        mPageDirection = PageDirection.parse(direction);
                    } else if (tag.equals("itemref")) {
                        String properties = parser.getAttributeValue(null, "properties");
                        String idref = parser.getAttributeValue(null, "idref");

                        if (mItems.containsKey(idref)) {
                            OpfItem it = mItems.get(idref);
                            it.setSpineProperties(properties);
                            spineItems.add(it);
                        } else {
                            throw new ContentsOtherException(
                                    "Spine item " + idref + " is not found");
                        }
                    }
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            throw new EpubParseException("parser error", e);
        } catch (IOException e) {
            throw new ContentsOtherException("i/o error", e);
        }

        for (OpfItem item : fallbackMap.keySet()) {
            item.setFallback(mItems.get(fallbackMap.get(item)));
        }

        // 単一ページ・見開きページを解析
        setSinglePagesFromItems(spineItems);
        setSpreadPagesFromItems(spineItems);

        // 目次を生成
        for (String key : mItems.keySet()) {
            OpfItem item = mItems.get(key);
            if (item.isNav()) {
                parseNav(reader, item);
            }
        }
        if (mNavList.isEmpty() && tocId != null && mItems.containsKey(tocId)) {
            String tocxml = StringUtil.trimBOM(
                    new String(reader.getFileContents(getFileName(mItems.get(tocId)))));
            parseToc(tocxml);
        }

        if (mItems.size() > 0
                && getSinglePageInfo().size() > 0 && getSpreadPageInfo().size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * spineに記載されたitemrefをもとに、単一ページリストを作成する
     * 
     * @param items
     */
    protected void setSinglePagesFromItems(ArrayList<OpfItem> items) {
        // 単一ページはそのままspineの内容を入れれば良い
        int currentPage = 0;
        for (OpfItem item : items) {
            currentPage = addSinglePageInfo(item, currentPage);
        }
    }

    /**
     * spineに記載されたitemrefをもとに、見開きページリストを作成する
     * 
     * @param items
     */
    protected void setSpreadPagesFromItems(ArrayList<OpfItem> items) {
        // 見開きページは、左右でまとめる
        OpfItem tmpLeft = null;
        OpfItem tmpRight = null;
        int currentPage = 0;
        for (OpfItem item : items) {
            switch (item.getPageSpread()) {
                case NONE:
                    if (isRtl()) {
                        if (tmpLeft != null) {
                            currentPage = addSpreadPageInfo(tmpLeft, tmpRight, currentPage);
                            tmpLeft = null;
                            tmpRight = item;
                        } else {
                            if (tmpRight != null) {
                                tmpLeft = item;
                            } else {
                                tmpRight = item;
                            }
                        }
                    } else {
                        if (tmpRight != null) {
                            currentPage = addSpreadPageInfo(tmpLeft, tmpRight, currentPage);
                            tmpLeft = item;
                            tmpRight = null;
                        } else {
                            if (tmpLeft != null) {
                                tmpRight = item;
                            } else {
                                tmpLeft = item;
                            }
                        }
                    }
                    break;
                case CENTER:
                    currentPage = addSpreadPageInfo(tmpLeft, tmpRight, currentPage);
                    currentPage = addSpreadPageInfo(item, currentPage);
                    tmpLeft = null;
                    tmpRight = null;
                    break;
                case LEFT:
                    if (isRtl()) {
                        currentPage = addSpreadPageInfo(item, tmpRight, currentPage);
                        tmpRight = null;
                    } else {
                        currentPage = addSpreadPageInfo(tmpLeft, null, currentPage);
                        tmpLeft = item;
                    }
                    break;
                case RIGHT:
                    if (isRtl()) {
                        currentPage = addSpreadPageInfo(null, tmpRight, currentPage);
                        tmpRight = item;
                    } else {
                        currentPage = addSpreadPageInfo(tmpLeft, item, currentPage);
                        tmpLeft = null;
                    }
                    break;
            }
        }
        addSpreadPageInfo(tmpLeft, tmpRight, currentPage);
    }

    /**
     * META-INF/container.xmlを読み、適切なOPFファイルのパスを返す
     * 
     * @param containerXml
     * @param priorityName 指定すると、この名前のファイルを優先的に使う。nullの場合は先頭のものを使う
     * @return
     * @throws EpubParseException
     * @throws ContentsOtherException
     */
    private static String getOpfPath(String containerXml, String priorityName)
            throws EpubParseException, ContentsOtherException {
        List<String> paths = new ArrayList<String>();
        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(new StringReader(containerXml));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("rootfile")) {
                    String fullpath = parser.getAttributeValue(null, "full-path");
                    LogUtil.v("opf found %s", fullpath);
                    if (priorityName == null) {
                        return fullpath;
                    }
                    paths.add(fullpath);
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            throw new EpubParseException("parser error", e);
        } catch (IOException e) {
            throw new ContentsOtherException("i/o error", e);
        }

        for (String path : paths) {
            if (path.contains(priorityName)) {
                LogUtil.v("OPF path =%s", path);
                return path;
            }
        }
        if (paths.size() > 0) {
            String path = paths.get(0);
            LogUtil.v("OPF path =%s", path);
            return path;
        }
        return null;
    }

    /**
     * toc.ncx(EPUB2)をもとに目次を生成
     * 
     * @param xml
     * @throws EpubParseException
     * @throws ContentsOtherException
     */
    protected void parseToc(String xml) throws EpubParseException,
            ContentsOtherException {
        LogUtil.v();
        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setInput(new StringReader(xml));

            NavPoint nav = null;
            for (int event = parser.getEventType();
                    event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
                if (event == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if (tagName.equals("navPoint")) {
                        nav = new NavPoint();
                        nav.order = Integer.parseInt(parser.getAttributeValue(null, "playOrder"));
                    } else if (tagName.equals("text") && nav != null) {
                        nav.title = parser.nextText();
                    } else if (tagName.equals("content")) {
                        String filename = parser.getAttributeValue(null, "src");
                        nav.item = getItemFromUrl(filename);
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    String tagName = parser.getName();
                    if (tagName.equals("navPoint")) {
                        if (nav != null && nav.title != null && nav.item != null) {
                            mNavList.add(nav);
                            LogUtil.v("navPoint added. Order:%d title:%s ref:%s",
                                    nav.order, nav.title, nav.item.getHref());
                        }
                        nav = null;
                    }
                }
            }

            Collections.sort(mNavList, new Comparator<NavListItem>() {
                @Override
                public int compare(NavListItem nav1, NavListItem nav2) {
                    return ((NavPoint) nav1).order - ((NavPoint) nav2).order;
                }
            });
        } catch (XmlPullParserException e) {
            throw new EpubParseException("parser error", e);
        } catch (IOException e) {
            throw new ContentsOtherException("i/o error", e);
        }
    }

    /**
     * nav.xhtml(EPUB3)をもとに目次を生成
     * 
     * @param xml
     * @throws EpubParseException
     * @throws ContentsOtherException
     */
    protected void parseNav(ContentsReader reader, OpfItem navItem)
            throws EpubParseException, ContentsOtherException {
        LogUtil.v();
        XmlPullParser parser = Xml.newPullParser();
        // 現在有効になっているnavタイプの優先順位
        int navPriority = NAV_TYPE_DEFAULT;

        try {
            parser.setInput(new StringReader(StringUtil.trimBOM(
                    new String(reader.getFileContents(getFileName(navItem))))));

            int event = parser.getEventType();
            boolean inNav = false;
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.getName().equals("nav")) {
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        if (parser.getAttributeName(i).equals("type")) {
                            int priority = navPriorityOf(parser.getAttributeValue(i));
                            if (navPriority == NAV_TYPE_TOC) {
                                // 既に一番優先順位が高いnav要素を保持しているのでパース処理を中止
                                return;
                            } else if (navPriority < priority) {
                                // 今まで処理したnav要素より優先順位が高いものを発見したら一旦nav情報をクリアする
                                mNavList.clear();
                                navPriority = priority;
                                inNav = true;
                            }
                            break;
                        }
                    }
                    if (navPriority == NAV_TYPE_DEFAULT) {
                        // 初回のnav要素はtype属性が無い場合でも無条件で処理対象とする
                        navPriority = NAV_TYPE_NONE;
                        inNav = true;
                    }
                } else if (event == XmlPullParser.END_TAG && parser.getName().equals("nav")) {
                    inNav = false;
                    if (navPriority == NAV_TYPE_TOC) {
                        // 処理したnavが一番優先順位が高い要素だったらパース処理を中止
                        return;
                    }
                }

                // <li class="chapter" id="toc0" data-item-id="page0">
                // <a href="0.xhtml">見出しタイトル</a></li>
                if (inNav && event == XmlPullParser.START_TAG && parser.getName().equals("a")) {
                    NavPoint nav = new NavPoint();
                    String href = parser.getAttributeValue(null, "href");

                    // ラベルが付いていたら削除(<a href="0.xhtml#123"> → <a href="0.xhtml">)
                    int labelIndex = href.lastIndexOf('#');
                    if (labelIndex > -1) {
                        href = href.substring(0, labelIndex);
                    }
                    nav.order = mNavList.size();
                    nav.item = getItemFromUrl(navItem, href);
                    nav.title = parser.nextText();
                    if (nav.item != null) {
                        mNavList.add(nav);
                        LogUtil.v("navPoint added. Order:%d title:%s ref:%s",
                                nav.order, nav.title, nav.item.getHref());
                    } else {
                        LogUtil.w("parseNav item not found:%s", href);
                    }
                }
                event = parser.next();
            }
        } catch (XmlPullParserException e) {
            throw new EpubParseException("parser error", e);
        } catch (IOException e) {
            throw new ContentsOtherException("i/o error", e);
        }
    }

    private static int navPriorityOf(String name) {
        if ("toc".equals(name)) {
            return NAV_TYPE_TOC;
        } else if ("landmarks".equals(name)) {
            return NAV_TYPE_LANDMARKS;
        } else if ("page-list".equals(name)) {
            return NAV_TYPE_PAGE_LIST;
        }
        return NAV_TYPE_NONE;
    }

    private OpfItem getItemFromUrl(String uri) {
        String filename = uriToFilename(uri);
        for (String id : mItems.keySet()) {
            OpfItem item = mItems.get(id);
            String target = mOpfDir + File.separator + item.getHref();
            if (filename.equals(target)) {
                LogUtil.v("found! %s", target);
                return item;
            }
        }
        LogUtil.w("not found! %s", filename);
        return null;
    }

    /**
     * URL(相対パス)からItemを取得
     * 
     * @param baseItem 基準になるItem
     * @param url
     * @return
     * @throws IOException
     */
    private OpfItem getItemFromUrl(OpfItem baseItem, String url) {
        String path = new File(mOpfDir
                + File.separator
                + StringUtil.getParentPath(baseItem.getHref())
                + StringUtil.trimUri(url)).getAbsolutePath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        for (String id : mItems.keySet()) {
            OpfItem it = mItems.get(id);
            String target = mOpfDir + File.separator + it.getHref();
            if (path.equals(target)) {
                LogUtil.v("found! %s", target);
                return it;
            }
        }
        LogUtil.w("not found! %s", path);
        return null;
    }

    private String uriToFilename(String uri) {
        String result = mOpfDir + StringUtil.trimUri(uri);
        LogUtil.v("%s => %s", uri, result);
        return result;
    }
}
