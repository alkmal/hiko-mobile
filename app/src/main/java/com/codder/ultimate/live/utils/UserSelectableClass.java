package com.codder.ultimate.live.utils;

import androidx.annotation.Nullable;

import com.codder.ultimate.live.model.PkAudioLiveUserRoot;

public final class UserSelectableClass {

    private @Nullable PkAudioLiveUserRoot.UsersItem.SeatItem seatItem;
    private boolean isSelected;

    public UserSelectableClass(@Nullable PkAudioLiveUserRoot.UsersItem.SeatItem seatItem) {
        this.seatItem = seatItem;
        this.isSelected = false;
    }

    @Nullable
    public PkAudioLiveUserRoot.UsersItem.SeatItem getSeatItem() {
        return seatItem;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSelectableClass)) return false;
        UserSelectableClass that = (UserSelectableClass) o;

        String thisId = (this.seatItem != null) ? this.seatItem.getUserId() : null;
        String thatId = (that.seatItem != null) ? that.seatItem.getUserId() : null;

        if (thisId == null) {
            if (thatId != null) return false;
        } else if (!thisId.equals(thatId)) {
            return false;
        }

        return this.isSelected == that.isSelected;
    }

    @Override
    public int hashCode() {
        String userId = (seatItem != null) ? seatItem.getUserId() : null;
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (isSelected ? 1 : 0);
        return result;
    }
}

