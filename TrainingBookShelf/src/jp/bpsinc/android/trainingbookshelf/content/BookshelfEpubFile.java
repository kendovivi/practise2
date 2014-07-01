
package jp.bpsinc.android.trainingbookshelf.content;

import android.util.Xml;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import jp.bpsinc.android.chogazo.viewer.content.ContentsReader;
import jp.bpsinc.android.chogazo.viewer.content.PageItem;
import jp.bpsinc.android.chogazo.viewer.content.epub.EpubFile;
import jp.bpsinc.android.chogazo.viewer.content.epub.OpfItem;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.EpubParseException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class BookshelfEpubFile extends EpubFile {
    private OpfItem mCoverItem;

    public PageItem getCoverItem() {
        return mCoverItem;
    }

    @Override
    protected boolean parseOpf(ContentsReader reader, String opfXml) throws EpubParseException,
            ContentsOtherException {
        HashMap<String, OpfItem> items = new HashMap<String, OpfItem>();
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
                                reader, getOpfDir(), id, href, mediaType, properties);
                        items.put(id, item);

                        if (item.isCoverImage()) {
                            // カバー見つかったらすぐ終了
                            mCoverItem = item;
                            return true;
                        }
                    } else if (tag.equals("itemref")) {
                        String idref = parser.getAttributeValue(null, "idref");

                        if (items.containsKey(idref)) {
                            // カバーない場合は1枚目をカバーの変わりにする
                            mCoverItem = items.get(idref);
                            return true;
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
        return false;
    }
}
