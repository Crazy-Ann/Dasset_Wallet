package com.dasset.wallet.ui.dialog.builder;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.SpannedString;

import com.dasset.wallet.base.constant.Temp;
import com.dasset.wallet.base.dialog.BaseDialogBuilder;
import com.dasset.wallet.ui.dialog.ImagePromptDialog;


public class ImagePromptDialogBuilder extends BaseDialogBuilder<ImagePromptDialogBuilder> {

    private CharSequence title;
    private byte[] image;
    private CharSequence prompt;
    private CharSequence positiveButtonText;
    private CharSequence negativeButtonText;
    private CharSequence neutralButtonText;

    public ImagePromptDialogBuilder(FragmentManager fragmentManager, Class<? extends ImagePromptDialog> clazz) {
        super(fragmentManager, clazz);
    }

    public ImagePromptDialogBuilder setTitle(Context ctx, int titleResourceId) {
        title = ctx.getString(titleResourceId);
        return this;
    }

    public ImagePromptDialogBuilder setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    public ImagePromptDialogBuilder setImage(byte[] image) {
        this.image = image;
        return this;
    }

    public ImagePromptDialogBuilder setPrompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    public ImagePromptDialogBuilder setPrompt(Context ctx, int resourceId, Object... formatArgs) {
        this.prompt = Html.fromHtml(String.format(Html.toHtml(new SpannedString(ctx.getText(resourceId))), formatArgs));
        return this;
    }

    public ImagePromptDialogBuilder setPrompt(CharSequence message) {
        this.prompt = message;
        return this;
    }

    public ImagePromptDialogBuilder setPositiveButtonText(Context ctx, int textResourceId) {
        this.positiveButtonText = ctx.getString(textResourceId);
        return this;
    }

    public ImagePromptDialogBuilder setPositiveButtonText(CharSequence text) {
        this.positiveButtonText = text;
        return this;
    }

    public ImagePromptDialogBuilder setNegativeButtonText(Context ctx, int textResourceId) {
        this.negativeButtonText = ctx.getString(textResourceId);
        return this;
    }

    public ImagePromptDialogBuilder setNegativeButtonText(CharSequence text) {
        negativeButtonText = text;
        return this;
    }

    public ImagePromptDialogBuilder setNeutralButtonText(Context ctx, int textResourceId) {
        this.neutralButtonText = ctx.getString(textResourceId);
        return this;
    }

    public ImagePromptDialogBuilder setNeutralButtonText(CharSequence text) {
        this.neutralButtonText = text;
        return this;
    }

    @Override
    protected Bundle prepareArguments() {
        Bundle bundle = new Bundle();
        bundle.putCharSequence(Temp.DIALOG_TITLE.getContent(), title);
        bundle.putByteArray(Temp.DIALOG_PROMPT_IMAGE.getContent(), image);
        bundle.putCharSequence(Temp.DIALOG_PROMPT.getContent(), prompt);
        bundle.putCharSequence(Temp.DIALOG_BUTTON_POSITIVE.getContent(), positiveButtonText);
        bundle.putCharSequence(Temp.DIALOG_BUTTON_NEGATIVE.getContent(), negativeButtonText);
        bundle.putCharSequence(Temp.DIALOG_BUTTON_NEUTRAL.getContent(), neutralButtonText);
        return bundle;
    }

    @Override
    protected ImagePromptDialogBuilder self() {
        return this;
    }
}
