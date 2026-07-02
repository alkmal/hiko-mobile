package com.codder.ultimate.modelclass;


public class SeatModalClass {

    String seat_id;
    String Image;
    private String name;
    boolean isReserved;

    public SeatModalClass(String seat_id, String image, String name, boolean isReserve) {
        this.seat_id = seat_id;
        Image = image;
        this.name = name;
        isReserved = isReserve;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }

    public String getSeat_id() {
        return seat_id;
    }

    public void setSeat_id(String seat_id) {
        this.seat_id = seat_id;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeatModalClass seat = (SeatModalClass) o;
        return Image == seat.Image &&
                isReserved == seat.isReserved &&
                seat_id.equals(seat.seat_id) &&
                (name == null ? seat.name == null : name.equals(seat.name));
    }

    @Override
    public int hashCode() {
        int result = seat_id.hashCode();
        result = Integer.parseInt(31 * result + Image);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (isReserved ? 1 : 0);
        return result;
    }


}
