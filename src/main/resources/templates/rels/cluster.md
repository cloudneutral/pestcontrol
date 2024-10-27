# Documentation for link rel 'cluster'

## Description

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc vel viverra urna. Nulla bibendum viverra pellentesque.
Morbi elementum maximus scelerisque. Donec magna diam, ultricies vitae arcu sed, facilisis egestas urna. Praesent nisl
mi, molestie non dignissim et, mattis a lectus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per
inceptos himenaeos. Sed mollis quam id erat cursus aliquet. Cras maximus dictum elit sit amet tristique. Donec porta
aliquet libero eget facilisis. Donec gravida sapien dui. Interdum et malesuada fames ac ante ipsum primis in faucibus.

## Actions

| Method | Description    |
|--------|----------------|
| GET    | View a cluster |

## Properties

| Property  | Description                          | Type       | Constraints |  
|-----------|--------------------------------------|------------|-------------|
| version   | The CockroachDB version string.      | String     |             |  
| available | Indicator for cluster availability.  | boolean    | Unique      |  
| nodes     | A unique identifier for this client. | Collection | Unique      |  

