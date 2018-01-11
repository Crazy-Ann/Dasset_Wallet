package com.dasset.wallet.ui.dialog.builder;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.SpannedString;

import com.dasset.wallet.base.constant.Temp;
import com.dasset.wallet.base.dialog.BaseDialogBuilder;
import com.dasset.wallet.ui.dialog.DownloadDialog;

import java.io.File;


public class DownloadDialogBuilder extends BaseDialogBuilder<DownloadDialogBuilder> {

    private CharSequence title;
    private CharSequence prompt;
    private String url;
    private File file;
    private CharSequence positiveButtonText;
    private CharSequence negativeButtonText;

    public DownloadDialogBuilder(FragmentManager fragmentManager, Class<? extends DownloadDialog> clazz) {
        super(fragmentManager, clazz);
    }

    public DownloadDialogBuilder setTitle(Context ctx, int titleResourceId) {
        this.title = ctx.getString(titleResourceId);
        return this;
    }

    public DownloadDialogBuilder setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    public DownloadDialogBuilder setPrompt(Context ctx, int messageResourceId) {
        this.prompt = ctx.getText(messageResourceId);
        return this;
    }

    public DownloadDialogBuilder setPrompt(Context ctx, int resourceId, Object... formatArgs) {
        this.prompt = Html.fromHtml(String.format(Html.toHtml(new SpannedString(ctx.getText(resourceId))), formatArgs));
        return this;
    }

    public DownloadDialogBuilder setPrompt(CharSequence message) {
        this.prompt = message;
        return this;
    }

    public DownloadDialogBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public DownloadDialogBuilder setFile(File file) {
        this.file = file;
        return this;
    }

    public DownloadDialogBuilder setPositiveButtonText(Context ctx, int textResourceId) {
        this.positiveButtonText = ctx.getString(textResourceId);
        return this;
    }

    public DownloadDialogBuilder setPositiveButtonText(CharSequence text) {
        this.positiveButtonText = text;
        return this;
    }

    public DownloadDialogBuilder setPositiveButtonText(String text) {
        this.positiveButtonText = text;
        return this;
    }

    public DownloadDialogBuilder setNegativeButtonText(Context ctx, int textResourceId) {
        this.negativeButtonText = ctx.getString(textResourceId);
        return this;
    }

    public DownloadDialogBuilder setNegativeButtonText(CharSequence text) {
        this.negativeButtonText = text;
        return this;
    }

    public DownloadDialogBuilder setNegativeButtonText(String text) {
        this.negativeButtonText = text;
        return this;
    }

    @Override
    protected Bundle prepareArguments() {
        Bundle bundle = new Bundle();
        bundle.putCharSequence(Temp.DIALOG_TITLE.getContent(), title);
        bundle.putCharSequence(Temp.DIALOG_PROMPT.getContent(), prompt);
        bundle.putString(Temp.DIALOG_DOWNLOAD_URL.getContent(), url);
        bundle.putSerializable(Temp.DIALOG_DOWNLOAD_FILE.getContent(), file);
        bundle.putCharSequence(Temp.DIALOG_BUTTON_POSITIVE.getContent(), positiveButtonText);
        bundle.putCharSequence(Temp.DIALOG_BUTTON_NEGATIVE.getContent(), negativeButtonText);
        return bundle;
    }

    @Override
    protected DownloadDialogBuilder self() {
        return this;
    }
}
