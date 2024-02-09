Feature: Admin Manage Delivery Locations

  Scenario: Admin can add a new delivery location
    Given I am logged in as an admin
    When I attempt to add a delivery location with the name "Polytech Kfet"
    Then The delivery location should exist

  Scenario: Admin can edit a delivery location
    Given I am logged in as an admin
    And I have a delivery location with the name "Polytech, 06410 Biot, France"
    When I attempt to edit the delivery location with the name "Polytech, 06410 Biot, France" to "Polytech Kfet"
    Then The delivery location should exist with the name "Polytech Kfet"

  Scenario: Admin can delete a delivery location
    Given I am logged in as an admin
    And I have a delivery location with the name "Polytech, 06410 Biot, France"
    When I attempt to delete the delivery location with the name "Polytech, 06410 Biot, France"
    Then The delivery location "Polytech, 06410 Biot, France" should not exist
