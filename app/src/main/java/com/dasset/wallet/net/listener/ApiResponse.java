package com.dasset.wallet.net.listener;

import com.dasset.wallet.base.http.model.BaseEntity;

public interface ApiResponse {

    void success(BaseEntity baseEntity);

    void failed(BaseEntity baseEntity);

}
