package com.dasset.wallet.core.qrcode;


import com.dasset.wallet.core.utils.Utils;

import java.util.List;

public class QRCodeTransportPage {
    private int mCurrentPage;
    private int mSumPage;
    private String mContent;

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void setCurrentPage(int mCurrentPage) {
        this.mCurrentPage = mCurrentPage;
    }

    public int getSumPage() {
        return mSumPage;
    }

    public void setSumPage(int mSumPage) {
        this.mSumPage = mSumPage;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public static String qrCodeTransportToString(
            List<QRCodeTransportPage> qrCodeTransportPages) {
        String transportString = "";
        for (QRCodeTransportPage qCodetTransportPage : qrCodeTransportPages) {
            if (!Utils.isEmpty(qCodetTransportPage.getContent())) {
                transportString = transportString
                        + qCodetTransportPage.getContent();
            }
        }
        return QRCodeUtil.decodeQrCodeString(transportString);
    }

    public static QRCodeTransportPage formatQrCodeTransport(String text) {
        if (!QRCodeUtil.verifyBitherQRCode(text)) {
            return null;
        }
        QRCodeTransportPage qrCodetTransportPage = new QRCodeTransportPage();
        String[] strArray = QRCodeUtil.split(text);
        if (Utils.isInteger(strArray[0]) && Utils.isInteger(strArray[1])) {
            int length = strArray[0].length() + strArray[1].length() + 2;
            qrCodetTransportPage.setSumPage(Integer.valueOf(strArray[0]) + 1);
            qrCodetTransportPage.setCurrentPage(Integer.valueOf(strArray[1]));
            qrCodetTransportPage.setContent(text.substring(length));
        } else {
            qrCodetTransportPage.setContent(text);
        }
        return qrCodetTransportPage;
    }


}
