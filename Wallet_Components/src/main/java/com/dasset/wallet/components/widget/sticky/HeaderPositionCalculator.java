package com.dasset.wallet.components.widget.sticky;

import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.dasset.wallet.components.widget.sticky.listener.HeaderProvider;
import com.dasset.wallet.components.widget.sticky.listener.OrientationProvider;
import com.dasset.wallet.components.widget.sticky.listener.StickyRecyclerHeadersAdapter;


/**
 * Calculates the position and location of header views
 */
public class HeaderPositionCalculator {

    private final StickyRecyclerHeadersAdapter stickyRecyclerHeadersAdapter;
    private final OrientationProvider orientationProvider;
    private final HeaderProvider headerProvider;
    private final DimensionCalculator dimensionCalculator;

    /**
     * The following fields are used as buffers for internal calculations. Their sole purpose is to avoid
     * allocating new Rect every time we need one.
     */
    private final Rect mTempRect1 = new Rect();
    private final Rect mTempRect2 = new Rect();

    public HeaderPositionCalculator(StickyRecyclerHeadersAdapter adapter, HeaderProvider headerProvider,
                                    OrientationProvider orientationProvider, DimensionCalculator dimensionCalculator) {
        stickyRecyclerHeadersAdapter = adapter;
        this.headerProvider = headerProvider;
        this.orientationProvider = orientationProvider;
        this.dimensionCalculator = dimensionCalculator;
    }

    /**
     * Determines if a view should have a sticky header.
     * The view has a sticky header if:
     * 1. It is the first element in the recycler view
     * 2. It has a valid ID associated to its position
     *
     * @param itemView    given by the RecyclerView
     * @param orientation of the Recyclerview
     * @param position    of the list item in question
     *
     * @return True if the view should have a sticky header
     */
    public boolean hasStickyHeader(View itemView, int orientation, int position) {
        int offset, margin;
        dimensionCalculator.initMargins(mTempRect1, itemView);
        if (orientation == LinearLayout.VERTICAL) {
            offset = itemView.getTop();
            margin = mTempRect1.top;
        } else {
            offset = itemView.getLeft();
            margin = mTempRect1.left;
        }

        return offset <= margin && stickyRecyclerHeadersAdapter.getHeaderId(position) >= 0;
    }

    /**
     * Determines if an item in the list should have a header that is different than the item in the
     * list that immediately precedes it. Items with no headers will always return false.
     *
     * @param position        of the list item in questions
     * @param isReverseLayout TRUE if layout manager has flag isReverseLayout
     *
     * @return true if this item has a different header than the previous item in the list
     *
     * @see {@link StickyRecyclerHeadersAdapter#getHeaderId(int)}
     */
    public boolean hasNewHeader(int position, boolean isReverseLayout) {
        if (indexOutOfBounds(position)) {
            return false;
        }

        long headerId = stickyRecyclerHeadersAdapter.getHeaderId(position);

        if (headerId < 0) {
            return false;
        }

        long nextItemHeaderId = -1;
        int nextItemPosition = position + (isReverseLayout ? 1 : -1);
        if (!indexOutOfBounds(nextItemPosition)) {
            nextItemHeaderId = stickyRecyclerHeadersAdapter.getHeaderId(nextItemPosition);
        }
        int firstItemPosition = isReverseLayout ? stickyRecyclerHeadersAdapter.getItemCount() - 1 : 0;

        return position == firstItemPosition || headerId != nextItemHeaderId;
    }

    private boolean indexOutOfBounds(int position) {
        return position < 0 || position >= stickyRecyclerHeadersAdapter.getItemCount();
    }

    public void initHeaderBounds(Rect bounds, RecyclerView recyclerView, View header, View firstView, boolean firstHeader) {
        int orientation = orientationProvider.getOrientation(recyclerView);
        initDefaultHeaderOffset(bounds, recyclerView, header, firstView, orientation);

        if (firstHeader && isStickyHeaderBeingPushedOffscreen(recyclerView, header)) {
            View viewAfterNextHeader = getFirstViewUnobscuredByHeader(recyclerView, header);
            int firstViewUnderHeaderPosition = recyclerView.getChildAdapterPosition(viewAfterNextHeader);
            View secondHeader = headerProvider.getHeader(recyclerView, firstViewUnderHeaderPosition);
            translateHeaderWithNextHeader(recyclerView, orientationProvider.getOrientation(recyclerView), bounds,
                                          header, viewAfterNextHeader, secondHeader);
        }
    }

    private void initDefaultHeaderOffset(Rect headerMargins, RecyclerView recyclerView, View header, View firstView, int orientation) {
        int translationX, translationY;
        dimensionCalculator.initMargins(mTempRect1, header);
        if (orientation == LinearLayoutManager.VERTICAL) {
            translationX = firstView.getLeft() + mTempRect1.left;
            translationY = Math.max(
                    firstView.getTop() - header.getHeight() - mTempRect1.bottom,
                    getListTop(recyclerView) + mTempRect1.top);
        } else {
            translationY = firstView.getTop() + mTempRect1.top;
            translationX = Math.max(
                    firstView.getLeft() - header.getWidth() - mTempRect1.right,
                    getListLeft(recyclerView) + mTempRect1.left);
        }

        headerMargins.set(translationX, translationY, translationX + header.getWidth(),
                          translationY + header.getHeight());
    }

    private boolean isStickyHeaderBeingPushedOffscreen(RecyclerView recyclerView, View stickyHeader) {
        View viewAfterHeader = getFirstViewUnobscuredByHeader(recyclerView, stickyHeader);
        if (viewAfterHeader != null) {
            int firstViewUnderHeaderPosition = recyclerView.getChildAdapterPosition(viewAfterHeader);
            if (firstViewUnderHeaderPosition == RecyclerView.NO_POSITION) {
                return false;
            }
            boolean isReverseLayout = orientationProvider.isReverseLayout(recyclerView);
            if (firstViewUnderHeaderPosition > 0 && hasNewHeader(firstViewUnderHeaderPosition, isReverseLayout)) {
                View nextHeader = headerProvider.getHeader(recyclerView, firstViewUnderHeaderPosition);
                dimensionCalculator.initMargins(mTempRect1, nextHeader);
                dimensionCalculator.initMargins(mTempRect2, stickyHeader);

                if (orientationProvider.getOrientation(recyclerView) == LinearLayoutManager.VERTICAL) {
                    int topOfNextHeader = viewAfterHeader.getTop() - mTempRect1.bottom - nextHeader.getHeight() - mTempRect1.top;
                    int bottomOfThisHeader = recyclerView.getPaddingTop() + stickyHeader.getBottom() + mTempRect2.top + mTempRect2.bottom;
                    if (topOfNextHeader < bottomOfThisHeader) {
                        return true;
                    }
                } else {
                    int leftOfNextHeader = viewAfterHeader.getLeft() - mTempRect1.right - nextHeader.getWidth() - mTempRect1.left;
                    int rightOfThisHeader = recyclerView.getPaddingLeft() + stickyHeader.getRight() + mTempRect2.left + mTempRect2.right;
                    if (leftOfNextHeader < rightOfThisHeader) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void translateHeaderWithNextHeader(RecyclerView recyclerView, int orientation, Rect translation,
                                               View currentHeader, View viewAfterNextHeader, View nextHeader) {
        dimensionCalculator.initMargins(mTempRect1, nextHeader);
        dimensionCalculator.initMargins(mTempRect2, currentHeader);
        if (orientation == LinearLayoutManager.VERTICAL) {
            int topOfStickyHeader = getListTop(recyclerView) + mTempRect2.top + mTempRect2.bottom;
            int shiftFromNextHeader = viewAfterNextHeader.getTop() - nextHeader.getHeight() - mTempRect1.bottom - mTempRect1.top - currentHeader.getHeight() - topOfStickyHeader;
            if (shiftFromNextHeader < topOfStickyHeader) {
                translation.top += shiftFromNextHeader;
            }
        } else {
            int leftOfStickyHeader = getListLeft(recyclerView) + mTempRect2.left + mTempRect2.right;
            int shiftFromNextHeader = viewAfterNextHeader.getLeft() - nextHeader.getWidth() - mTempRect1.right - mTempRect1.left - currentHeader.getWidth() - leftOfStickyHeader;
            if (shiftFromNextHeader < leftOfStickyHeader) {
                translation.left += shiftFromNextHeader;
            }
        }
    }

    /**
     * Returns the first item currently in the RecyclerView that is not obscured by a header.
     *
     * @param parent Recyclerview containing all the list items
     *
     * @return first item that is fully beneath a header
     */
    private View getFirstViewUnobscuredByHeader(RecyclerView parent, View firstHeader) {
        boolean isReverseLayout = orientationProvider.isReverseLayout(parent);
        int step = isReverseLayout ? -1 : 1;
        int from = isReverseLayout ? parent.getChildCount() - 1 : 0;
        for (int i = from; i >= 0 && i <= parent.getChildCount() - 1; i += step) {
            View child = parent.getChildAt(i);
            if (!itemIsObscuredByHeader(parent, child, firstHeader, orientationProvider.getOrientation(parent))) {
                return child;
            }
        }
        return null;
    }

    /**
     * Determines if an item is obscured by a header
     *
     * @param parent
     * @param item        to determine if obscured by header
     * @param header      that might be obscuring the item
     * @param orientation of the {@link RecyclerView}
     *
     * @return true if the item view is obscured by the header view
     */
    private boolean itemIsObscuredByHeader(RecyclerView parent, View item, View header, int orientation) {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) item.getLayoutParams();
        dimensionCalculator.initMargins(mTempRect1, header);

        int adapterPosition = parent.getChildAdapterPosition(item);
        if (adapterPosition == RecyclerView.NO_POSITION || headerProvider.getHeader(parent, adapterPosition) != header) {
            // Resolves https://github.com/timehop/sticky-headers-recyclerview/issues/36
            // Handles an edge case where a trailing header is smaller than the current sticky header.
            return false;
        }

        if (orientation == LinearLayoutManager.VERTICAL) {
            int itemTop = item.getTop() - layoutParams.topMargin;
            int headerBottom = header.getBottom() + mTempRect1.bottom + mTempRect1.top;
            if (itemTop > headerBottom) {
                return false;
            }
        } else {
            int itemLeft = item.getLeft() - layoutParams.leftMargin;
            int headerRight = header.getRight() + mTempRect1.right + mTempRect1.left;
            if (itemLeft > headerRight) {
                return false;
            }
        }

        return true;
    }

    private int getListTop(RecyclerView view) {
        if (view.getLayoutManager().getClipToPadding()) {
            return view.getPaddingTop();
        } else {
            return 0;
        }
    }

    private int getListLeft(RecyclerView view) {
        if (view.getLayoutManager().getClipToPadding()) {
            return view.getPaddingLeft();
        } else {
            return 0;
        }
    }
}
