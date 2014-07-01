
package jp.bpsinc.android.chogazo.viewer.content;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

import android.content.Context;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsUnzipException;
import jp.bpsinc.android.chogazo.viewer.exception.DrmException;
import jp.bpsinc.android.chogazo.viewer.exception.UnexpectedException;

/**
 * コンテンツの実ファイル（ZIPまたはDRMパッケージになっている）
 */
public interface ContentsReader {
    /**
     * コンテンツをオープンする
     * 
     * @param context コンテキスト
     * @param path ファイルパス
     * @param option その他DRM処理などで必要なデータがある場合に使用
     * @throws FileNotFoundException ファイルが存在しない
     * @throws DrmException DRM解除に失敗
     * @throws ContentsUnzipException コンテンツの解凍に失敗
     * @throws ContentsOtherException その他の理由によりコンテンツのオープンに失敗
     * @throws UnexpectedException その他例外
     */
    public void open(Context context, String path, Serializable option) throws
            FileNotFoundException, DrmException, ContentsUnzipException,
            ContentsOtherException, UnexpectedException;

    /**
     * コンテンツをクローズする
     */
    public void close();

    /**
     * コンテンツがクローズしているか判定
     * 
     * @return クローズしていたらtrue、クローズしていないならfalse
     */
    public boolean isClosed();

    /**
     * オープンしたコンテンツのパスを取得する
     * 
     * @return　オープンしたコンテンツのパス、クローズしていたらnull
     */
    public String getPath();

    /**
     * 指定ファイルの中身をバイト列で取得
     * 
     * @param key キー
     * @return 指定ファイルのバイト配列、指定ファイルが存在しない場合はnull
     * @throws ContentsOtherException クローズしている
     */
    public byte[] getFileContents(String key) throws ContentsOtherException;

    /**
     * 指定ファイルのサイズを取得
     * 
     * @param key キー
     * @return 指定ファイルのサイズ、closeしている場合や指定ファイルが存在しない場合は-1
     */
    public long getFileSize(String key);

    /**
     * 指定ファイルのInputStreamを取得
     * 
     * @param key キー
     * @return 指定ファイルのInputStream、指定ファイルが存在しない場合はnull
     * @throws ContentsOtherException クローズしている
     */
    public InputStream getInputStream(String key) throws ContentsOtherException;

    /**
     * 指定したファイルが存在するかどうかチェック
     * 
     * @param key キー
     * @return 指定ファイルが存在した場合はtrue、存在しない場合はfalse
     */
    public boolean hasFile(String key);
}
