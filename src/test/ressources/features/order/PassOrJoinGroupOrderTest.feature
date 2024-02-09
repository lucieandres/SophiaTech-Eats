Feature: Group order Feature

  Background:
    Given the customer "user1" "name1" with the password "password1"
    And he chooses the delivery date "23/11/2025 12:00:00"
    And he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    When he validates the group order
    Then the group order is registered in the system

  Scenario: Adding an order to the group order I initiated
    When he chooses the restaurant with id 1
    And he chooses 1 of the menu "Salade César"
    And he validates his suborder
    Then his order is registered in the group order

  Scenario: Adding an order to an existing group order
    Given the customer "user4" "name4" with the password "password4"
    When he chooses the restaurant with id 1
    And he chooses 1 of the menu "Salade César"
    And he validates his suborder
    Then his order is registered in the group order

  Scenario: Getting discount on a group order
    Given 10 connected users, each having added their suborder
    When his order is finished
    Then all users get a credit on future order

