Feature: Pass AfterWork order
  Scenario: Passing an after work order
    Given the customer "user1" "name1" with the password "password1"
    When he chooses the delivery date "24/11/2025 12:00:00"
    When he chooses the restaurant with id 1
    When he chooses the number of participant 15
    When he validates the after work order
    Then his after work order is registered in the system
    And he receives a confirmation notification
  Scenario: Passing an after work order with no participant
    Given the customer "user1" "name1" with the password "password1"
    When he chooses the delivery date "24/11/2025 12:00:00"
    When he chooses the restaurant with id 1
    When he chooses the number of participant 0
    When he validates the after work order
    Then an order error message should be printed

  Scenario: Passing an after work order with no referenced restaurant
    Given the customer "user1" "name1" with the password "password1"
    When he chooses the delivery date "24/11/2025 12:00:00"
    When he chooses the restaurant with id 1000
    When he chooses the number of participant 40
    When he validates the after work order
    Then an order error message should be printed
