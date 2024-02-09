Feature: Delivery Account Operations

  Scenario: View list of deliverable orders
    Given a delivery account "user1" "name1" with password "password1"
    When The system possess 3 order ready to deliver
    And I view the list of deliverable orders
    Then I should see 3 orders

  Scenario: Access restriction for not connected users
    Given a non registered delivery man
    When I am not connected
    Then I can't access the list of deliverable orders

  Scenario: View assigned order
    Given a delivery account "user1" "name1" with password "password1"
    When I assign myself to an order
    Then I should be able to view my assigned order
    And I receive a notification with the order details

  Scenario: No assigned order to view
    Given a delivery account "user1" "name1" with password "password1"
    When I'm not assigned to any order
    Then I should not be able to view my assigned order

  Scenario: Delivery time
    Given a delivery account "user1" "name1" with password "password1"
    When I'm doing a delivery
    Then I should take 10 minutes to finish it
