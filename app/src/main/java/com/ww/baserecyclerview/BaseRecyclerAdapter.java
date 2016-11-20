package com.ww.baserecyclerview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by wuwen on 2016/11/16.
 */

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_FOOTER = 2;
    public static final int TYPE_EMPTY = 3;
    private List<T> list = null;

    private View mHeaderView;
    private View mFooterView;
    private View mEmptyView;

    private OnItemClickListener mListener;
    public void setOnItemClickListener(OnItemClickListener li) {
        mListener = li;
    }

    public BaseRecyclerAdapter(List<T> list) {
        this.list = list;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public void setFooterView(View footerView) {
        mFooterView = footerView;
        if (mHeaderView == null) {
            notifyItemInserted(list.size());
        } else {
            notifyItemInserted(list.size() + 1);
        }
    }

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
        notifyItemInserted(0);
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public View getFooterView() {
        return mFooterView;
    }

    public void removeHeaderView() {
        mHeaderView = null;
        notifyDataSetChanged();
    }

    public void removeFooterView() {
        mFooterView = null;
        notifyDataSetChanged();
    }

    public void removeEmptyView() {
        mEmptyView = null;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (mEmptyView != null && list.size() == 0) {
            return TYPE_EMPTY;
        }
        if (mHeaderView == null) {
            if (mFooterView != null) {
                if (position == list.size()) return TYPE_FOOTER;
            }
        } else {
            if (position == 0) return TYPE_HEADER;
            if (mFooterView != null) {
                if (position == list.size() + 1) return TYPE_FOOTER;
            }
        }
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        if (mEmptyView != null && viewType == TYPE_EMPTY) return new Holder(mEmptyView);
        if (mHeaderView != null && viewType == TYPE_HEADER) return new Holder(mHeaderView);
        if (mFooterView != null && viewType == TYPE_FOOTER) return new Holder(mFooterView);
        return onCreate(parent, viewType);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == TYPE_EMPTY) return;
        if (getItemViewType(position) == TYPE_HEADER) return;
        if (getItemViewType(position) == TYPE_FOOTER) return;
        final int pos = getRealPosition(viewHolder);
        final T data = list.get(pos);
        onBind(viewHolder, pos, data);
        if (mListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClick(pos, data);
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == TYPE_HEADER || getItemViewType(position) == TYPE_FOOTER
                            ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            if (mHeaderView == null) {
                if (mFooterView != null) {
                    p.setFullSpan(holder.getLayoutPosition() == list.size());
                }
            } else {
                p.setFullSpan(holder.getLayoutPosition() == 0);
                if (mFooterView != null) {
                    p.setFullSpan(holder.getLayoutPosition() == list.size() + 1);
                }
            }
        }
    }

    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position - 1;
    }

    @Override
    public int getItemCount() {
        if (list.size() == 0 && mEmptyView != null) {
            return 1;
        } else if (mHeaderView == null) {
            if (mFooterView == null) {
                return list.size();
            } else {
                return list.size() + 1;
            }
        } else {
            if (mFooterView == null) {
                return list.size() + 1;
            } else {
                return list.size() + 2;
            }
        }
    }

    public abstract RecyclerView.ViewHolder onCreate(ViewGroup parent, final int viewType);

    public abstract void onBind(RecyclerView.ViewHolder viewHolder, int RealPosition, T data);

    public class Holder extends RecyclerView.ViewHolder {
        public Holder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener<T> {
        void onItemClick(int position, T data);
    }
}
