Feature: Delivery notification and confirmation

  Scenario: User receiving delivery notifications
    Given the customer "user1" "name1" with the password "password1" have made an order
    When his order is on the way
    Then he receives a notification


