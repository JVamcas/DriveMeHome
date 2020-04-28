package com.owambo.jvamcas.drivemehome.utils;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;


class ImageUtil {

    static RequestCreator requestCreator(Transformation transform, String iconUrl, int size, int default_icon) {
        RequestCreator mCreator = (iconUrl == null || iconUrl.isEmpty())? Picasso.get().load(default_icon):
                Picasso.get().load(iconUrl);
        mCreator.transform(transform)
                .centerCrop()
                .resize(size,size);
        return mCreator;
    }
}
