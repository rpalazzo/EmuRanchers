#!/usr/bin/env python3
from itertools import chain, combinations

def get_payment(cost, hand):
    c = chain.from_iterable((combinations(hand,r) for r in range(1, len(hand)+1)))
    l = list(c)

    if sum(hand) <= cost:
        return hand

    min_payment_cards = hand
    min_payment_amount = sum(hand)

    for i in l:
        if sum(i) >= cost and sum(i) < min_payment_amount:
            min_payment_amount = sum(i)
            min_payment_cards = i
    return list(min_payment_cards)

def main():

    print(get_payment(19, [9,8,7,3])) #[9,7,3] r0; skip a middle number
    print(get_payment(19, [9,8,6,5,3])) #[8,6,5] r0; don't include first or last
    print(get_payment(19, [9,8,5,3])) #[9,8,3] r1; skip a middle number
    print(get_payment(19, [5,4,3,2,1])) #[5,4,3,2,1] not enough; return entire array
    print(get_payment(19, [9,8,7,6,5,4,3,2,1])) #[9,8,2]


if __name__ == "__main__":
    main()