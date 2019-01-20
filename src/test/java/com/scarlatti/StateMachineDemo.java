package com.scarlatti;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * @author Alessandro Scarlatti
 * @since Saturday, 1/19/2019
 */
public class StateMachineDemo {

    @Test
    public void test() {
        CreditCardAccount creditCardAccount = new CreditCardAccount();

        assertThrows(IllegalStateException.class, ()-> {
            creditCardAccount.withdraw(60);
        });

        creditCardAccount.setCreditLimit(200);
        creditCardAccount.withdraw(20);
        creditCardAccount.withdraw(30);
        assertThrows(IllegalStateException.class, ()-> {
            creditCardAccount.acceptPayment(60);
        });
    }

    @SuppressWarnings("unchecked")
    public static <ThrowableType> ThrowableType assertThrows(Class<ThrowableType> clazz, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            assertTrue((clazz.isAssignableFrom(e.getClass())));
            return (ThrowableType) e;
        }

        throw new IllegalStateException("Expecting a throwable, but none was thrown.");
    }

    static class CreditCardAccount implements ICreditCard {
        private CreditCard creditCard;

        public CreditCardAccount() {
            creditCard = new NewCreditCard(this);
        }

        @Override
        public void setCreditLimit(int limit) {
            creditCard.setCreditLimit(limit);
        }

        @Override
        public void withdraw(int amount) {
            creditCard.withdraw(amount);
        }

        @Override
        public void acceptPayment(int amount) {
            creditCard.acceptPayment(amount);
        }
    }

    interface ICreditCard {
        default void setCreditLimit(int limit) {
            throw new IllegalStateException("May not set credit limit.");
        }

        default void withdraw(int amount) {
            throw new IllegalStateException("May not withdraw.");
        }

        default void acceptPayment(int amount) {
            throw new IllegalStateException("May not accept payment.");
        }
    }

    static class CreditCard implements ICreditCard {

        int limit;
        int balanceRemaining;
        Queue<Integer> unpaidTransactions = new ArrayDeque<>();
        CreditCardAccount account;

        public CreditCard(CreditCardAccount account) {
            this.account = account;
        }

        public CreditCard(CreditCard other) {
            this.limit = other.limit;
            this.balanceRemaining = other.balanceRemaining;
            this.unpaidTransactions = other.unpaidTransactions;
            this.account = other.account;
        }
    }

    static class NewCreditCard extends CreditCard {

        public NewCreditCard(CreditCardAccount account) {
            super(account);
        }

        public NewCreditCard(CreditCard other) {
            super(other);
        }

        @Override
        public void setCreditLimit(int limit) {
            this.limit = limit;
            this.balanceRemaining = limit;
            account.creditCard = new ReadyToUseCard(this);
        }
    }

    static class ReadyToUseCard extends CreditCard {

        public ReadyToUseCard(CreditCardAccount account) {
            super(account);
        }

        public ReadyToUseCard(CreditCard other) {
            super(other);
        }

        @Override
        public void withdraw(int amount) {
            if (unpaidTransactions.size() > 45) {
                throw new IllegalStateException("Must pay back some money before borrowing more.");
            }

            if (balanceRemaining > 0 && amount <= balanceRemaining) {
                balanceRemaining -= amount;
                unpaidTransactions.add(amount);
            } else {
                throw new IllegalStateException("Not enough money left on card.");
            }
        }

        @Override
        public void acceptPayment(int amount) {

            if (amount <= limit - balanceRemaining) {
                unpaidTransactions.poll();
                balanceRemaining += amount;
            } else {
                throw new IllegalStateException("Cannot pay more money back than already owed.");
            }
        }
    }
}
