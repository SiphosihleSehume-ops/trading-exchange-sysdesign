package za.co.eliteproject.enums;

public enum OrderStatus {
<<<<<<< HEAD
<<<<<<< HEAD
    PENDING, PARTAL, FILLED, CANCELLED, REJECTED;
=======
    PENDING, PARTIAL, FILLED, CANCELLED, REJECTED;
>>>>>>> main
=======
    PENDING, PARTIAL, FILLED, CANCELLED, REJECTED;
>>>>>>> main

    public boolean isTerminal() {
        switch (this) {
            case FILLED:
            case CANCELLED:
            case REJECTED:
                return true;
<<<<<<< HEAD
<<<<<<< HEAD
        }
        return false;
=======
            default:
                return false;
        }
>>>>>>> main
=======
            default:
                return false;
        }
>>>>>>> main
    }
}
